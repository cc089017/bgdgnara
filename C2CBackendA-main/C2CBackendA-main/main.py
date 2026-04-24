from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session

import models
import schemas
from database import Base, engine, get_db
from core.security import hash_password
from routers import auth, products, users, misc

# ── 앱 시작 시 테이블 자동 생성 ───────────────────────────
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="중고거래 플랫폼 API",
    description="secondhand_platform DB 연동 REST API",
    version="1.0.0",
)

# ── 라우터 등록 ─────────────────────────────────────────────
app.include_router(auth.router)
app.include_router(products.router)
app.include_router(users.router)
app.include_router(misc.router)


# ── POST /auth/register : 회원가입 ────────────────────────
@app.post(
    "/auth/register",
    response_model=schemas.RegisterResponse,
    status_code=status.HTTP_201_CREATED,
    summary="회원가입",
    tags=["Auth"],
)
def register(body: schemas.RegisterRequest, db: Session = Depends(get_db)):
    # 중복 아이디 체크
    existing = db.query(models.User).filter(models.User.user_id == body.user_id).first()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="이미 사용 중인 아이디입니다.",
        )

    new_user = models.User(
        user_id=body.user_id,
        user_pwd=hash_password(body.user_pwd),
        nickname=body.nickname,
        phone_num=body.phone_num,
        region=body.region,
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return schemas.RegisterResponse(
        message="회원가입이 완료되었습니다.",
        user_id=new_user.user_id,
    )


# ── 헬스체크 ──────────────────────────────────────────────
@app.get("/", tags=["Health"])
def root():
    return {"status": "ok", "message": "서버가 정상 실행 중입니다."}
