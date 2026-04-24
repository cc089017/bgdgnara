from typing import Optional, List
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File, Query, Response
from sqlalchemy.orm import Session, selectinload
from sqlalchemy import or_

import models
import schemas
from database import get_db
from routers.auth import get_current_user

router = APIRouter(tags=["Products"])

# ── GET /products : 상품 목록 조회 (필터, 검색, 페이지네이션) ──
@router.get("/products", response_model=List[schemas.ProductResponse])
def get_products(
    category: Optional[str] = None,
    search: Optional[str] = None,
    min_price: Optional[int] = None,
    max_price: Optional[int] = None,
    skip: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db)
):
    # 썸네일(thumbnail_url property)을 위한 images 이거 미리 로드 (N+1 방지)
    query = db.query(models.Product).options(selectinload(models.Product.images))

    if category:
        query = query.filter(models.Product.category == category)
    if search:
        query = query.filter(
            or_(
                models.Product.product_title.contains(search),
                models.Product.product_body.contains(search)
            )
        )
    if min_price is not None:
        query = query.filter(models.Product.product_price >= min_price)
    if max_price is not None:
        query = query.filter(models.Product.product_price <= max_price)

    products = query.offset(skip).limit(limit).all()
    return products


# ── POST /products : 상품 등록 ────────────────────────────────
@router.post("/products", response_model=schemas.ProductResponse, status_code=status.HTTP_201_CREATED)
def create_product(
    body: schemas.ProductCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    new_product = models.Product(
        user_id=current_user.user_id,
        product_title=body.product_title,
        product_body=body.product_body,
        product_price=body.product_price,
        category=body.category
    )
    db.add(new_product)
    db.commit()
    db.refresh(new_product)
    return new_product


# ── GET /products/me : 내가 올린 상품 목록 ──────────────────
@router.get("/products/me", response_model=List[schemas.ProductResponse])
def get_my_products(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return (
        db.query(models.Product)
        .options(selectinload(models.Product.images))
        .filter(models.Product.user_id == current_user.user_id)
        .all()
    )


# ── GET /products/{product_id} : 상품 상세 조회 (판매자 + 이미지 포함) ──
# 주의: /products/me 아래에 위치해야 함 ("me"가 product_id로 해석되는 것 방지)
@router.get("/products/{product_id}", response_model=schemas.ProductDetailResponse)
def get_product_detail(product_id: int, db: Session = Depends(get_db)):
    product = (
        db.query(models.Product)
        .options(selectinload(models.Product.images))
        .filter(models.Product.product_id == product_id)
        .first()
    )
    if not product:
        raise HTTPException(status_code=404, detail="상품을 찾을 수 없습니다.")

    # 이미지 URL 리스트 (image_order 순)
    images_sorted = sorted(product.images, key=lambda img: img.image_order)
    image_urls = [
        f"/products/{product.product_id}/images/{img.image_order}"
        for img in images_sorted
    ]

    seller = product.owner
    return schemas.ProductDetailResponse(
        product_id=product.product_id,
        user_id=product.user_id,
        product_status=product.product_status,
        product_title=product.product_title,
        product_body=product.product_body,
        product_price=product.product_price,
        category=product.category,
        thumbnail_url=product.thumbnail_url,
        seller_nickname=seller.nickname,
        seller_region=seller.region,
        image_urls=image_urls,
    )


# ── GET /products/{product_id}/images : 이미지 순서 목록 ───────
@router.get("/products/{product_id}/images")
def list_product_images(product_id: int, db: Session = Depends(get_db)):
    product = db.query(models.Product).filter(models.Product.product_id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="상품을 찾을 수 없습니다.")

    images = (
        db.query(models.ProductImage)
        .filter(models.ProductImage.product_id == product_id)
        .order_by(models.ProductImage.image_order)
        .all()
    )
    return {
        "product_id": product_id,
        "image_orders": [img.image_order for img in images],
        "image_urls": [
            f"/products/{product_id}/images/{img.image_order}" for img in images
        ],
    }


# ── GET /products/{product_id}/images/{image_order} : 이미지 바이너리 서빙 ──
@router.get("/products/{product_id}/images/{image_order}")
def get_product_image(
    product_id: int,
    image_order: int,
    db: Session = Depends(get_db)
):
    image = (
        db.query(models.ProductImage)
        .filter(
            models.ProductImage.product_id == product_id,
            models.ProductImage.image_order == image_order,
        )
        .first()
    )
    if not image:
        raise HTTPException(status_code=404, detail="이미지를 찾을 수 없습니다.")
    # JPEG/PNG 구분 없이 image/jpeg로 응답 (브라우저/Coil 모두 자동 인식)
    return Response(content=image.image_data, media_type="image/jpeg")


# ── PATCH /products/{product_id} : 상품 수정 ──────────────────
@router.patch("/products/{product_id}", response_model=schemas.ProductResponse)
def update_product(
    product_id: int,
    body: schemas.ProductUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    product = db.query(models.Product).filter(models.Product.product_id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="상품을 찾을 수 없습니다.")
    if product.user_id != current_user.user_id:
        raise HTTPException(status_code=403, detail="수정 권한이 없습니다.")
    
    update_data = body.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(product, key, value)
    
    db.commit()
    db.refresh(product)
    return product


# ── DELETE /products/{product_id} : 상품 삭제 ──────────────────
@router.delete("/products/{product_id}", response_model=schemas.MessageResponse)
def delete_product(
    product_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    product = db.query(models.Product).filter(models.Product.product_id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="상품을 찾을 수 없습니다.")
    if product.user_id != current_user.user_id:
        raise HTTPException(status_code=403, detail="삭제 권한이 없습니다.")
    
    db.delete(product)
    db.commit()
    return schemas.MessageResponse(message="상품이 삭제되었습니다.")


# ── POST /products/{product_id}/images : 상품 이미지 업로드 ──
@router.post("/products/{product_id}/images", response_model=schemas.MessageResponse)
async def upload_product_images(
    product_id: int,
    files: List[UploadFile] = File(...),
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    product = db.query(models.Product).filter(models.Product.product_id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="상품을 찾을 수 없습니다.")
    if product.user_id != current_user.user_id:
        raise HTTPException(status_code=403, detail="권한이 없습니다.")
    
    for idx, file in enumerate(files):
        content = await file.read()
        new_image = models.ProductImage(
            product_id=product_id,
            image_data=content,
            image_order=idx
        )
        db.add(new_image)
    
    db.commit()
    return schemas.MessageResponse(message=f"{len(files)}개의 이미지가 업로드되었습니다.")


# ── PATCH /products/{product_id}/status : 상태 변경 ──────────
@router.patch("/products/{product_id}/status", response_model=schemas.ProductResponse)
def update_product_status(
    product_id: int,
    body: schemas.ProductStatusUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    product = db.query(models.Product).filter(models.Product.product_id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="상품을 찾을 수 없습니다.")
    if product.user_id != current_user.user_id:
        raise HTTPException(status_code=403, detail="권한이 없습니다.")
    
    product.product_status = body.status
    db.commit()
    db.refresh(product)
    return product


# ── GET /products/{product_id}/related : 연관 상품 ────────────
@router.get("/products/{product_id}/related", response_model=List[schemas.ProductResponse])
def get_related_products(
    product_id: int,
    limit: int = 5,
    db: Session = Depends(get_db)
):
    target = db.query(models.Product).filter(models.Product.product_id == product_id).first()
    if not target:
        raise HTTPException(status_code=404, detail="상품을 찾을 수 없습니다.")
    
    related = (
        db.query(models.Product)
        .filter(models.Product.category == target.category)
        .filter(models.Product.product_id != product_id)
        .limit(limit)
        .all()
    )
    return related


# ── GET /search : 통합 검색 (상품 + 유저) ────────────────────
@router.get("/search", response_model=schemas.IntegratedSearchResponse)
def integrated_search(
    q: str = Query(..., min_length=1),
    db: Session = Depends(get_db)
):
    # 상품 검색
    products = (
        db.query(models.Product)
        .filter(
            or_(
                models.Product.product_title.contains(q),
                models.Product.product_body.contains(q)
            )
        )
        .limit(10)
        .all()
    )
    
    # 유저 검색 (닉네임 또는 아이디)
    users = (
        db.query(models.User)
        .filter(
            or_(
                models.User.nickname.contains(q),
                models.User.user_id.contains(q)
            )
        )
        .limit(10)
        .all()
    )
    
    return {
        "products": products,
        "users": users
    }
