from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

import models
import schemas
from database import get_db
from routers.auth import get_current_user

router = APIRouter(prefix="/users", tags=["Users"])

# ── GET /users/me : 내 프로필 조회 (로그인 상태) ──────────────
@router.get("/me", response_model=schemas.UserResponse)
def get_my_profile(current_user: models.User = Depends(get_current_user)):
    return current_user


# ── PATCH /users/me : 내 프로필 수정 (로그인 상태) ────────────
@router.patch("/me", response_model=schemas.UserResponse)
def update_my_profile(
    user_update: schemas.UserUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    update_data = user_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(current_user, key, value)
    
    db.commit()
    db.refresh(current_user)
    return current_user


# ── DELETE /users/me : 회원 탈퇴 (로그인 상태) ────────────────
@router.delete("/me", response_model=schemas.MessageResponse)
def delete_account(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    db.delete(current_user)
    db.commit()
    return schemas.MessageResponse(message="회원 탈퇴가 완료되었습니다.")


# ── GET /users/{user_id} : 특정 유저 프로필 조회 ──────────────
@router.get("/{user_id}", response_model=schemas.UserResponse)
def get_user_profile(user_id: str, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="유저를 찾을 수 없습니다.")
    return user


# ── GET /users/{user_id}/products : 특정 유저의 판매 상품 ────
@router.get("/{user_id}/products", response_model=List[schemas.ProductResponse])
def get_user_products(user_id: str, db: Session = Depends(get_db)):
    products = db.query(models.Product).filter(models.Product.user_id == user_id).all()
    return products


# ── GET /users/{user_id}/reviews : 특정 유저가 받은 리뷰 ──────
@router.get("/{user_id}/reviews")
def get_user_reviews(user_id: str):
    # 리뷰 테이블이 아직 없으므로 고정된 데이터를 반환합니다.
    return {
        "user_id": user_id,
        "reviews": [
            {"id": 1, "content": "친절해요", "rating": 5},
            {"id": 2, "content": "응답이 빨라요", "rating": 4}
        ]
    }
