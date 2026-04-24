from datetime import datetime, timedelta, timezone

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session

import models
import schemas
from database import get_db
from core.security import (
    hash_password,
    verify_password,
    create_access_token,
    create_refresh_token,
    decode_token,
    REFRESH_TOKEN_EXPIRE_DAYS,
)

router = APIRouter(prefix="/auth", tags=["Auth"])

# Authorization 헤더에서 Bearer 토큰 추출
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")


# ── 공통: 액세스 토큰으로 현재 유저 조회 ─────────────────────
def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(get_db),
) -> models.User:
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="유효하지 않거나 만료된 토큰입니다.",
        headers={"WWW-Authenticate": "Bearer"},
    )
    payload = decode_token(token)
    if payload is None or payload.get("type") != "access":
        raise credentials_exception

    user_id: str = payload.get("sub")
    if not user_id:
        raise credentials_exception

    user = db.query(models.User).filter(models.User.user_id == user_id).first()
    if user is None:
        raise credentials_exception
    return user


# ── POST /auth/login : 로그인 (JWT 발급) ─────────────────────
@router.post(
    "/login",
    response_model=schemas.LoginResponse,
    summary="로그인 (JWT 발급)",
)
def login(body: schemas.LoginRequest, db: Session = Depends(get_db)):
    # 유저 확인
    user = db.query(models.User).filter(models.User.user_id == body.user_id).first()
    if not user or not verify_password(body.user_pwd, user.user_pwd):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="아이디 또는 비밀번호가 올바르지 않습니다.",
        )

    # 토큰 생성
    access_token = create_access_token(data={"sub": user.user_id})
    refresh_token = create_refresh_token(data={"sub": user.user_id})

    # 리프레시 토큰 DB 저장
    expires_at = datetime.now(timezone.utc) + timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)
    db_token = models.RefreshToken(
        user_id=user.user_id,
        token=refresh_token,
        expires_at=expires_at,
    )
    db.add(db_token)
    db.commit()

    return schemas.LoginResponse(
        message="로그인 성공",
        access_token=access_token,
        refresh_token=refresh_token,
    )


# ── POST /auth/logout : 로그아웃 (토큰 무효화) ───────────────
@router.post(
    "/logout",
    response_model=schemas.MessageResponse,
    summary="로그아웃 (토큰 무효화)",
)
def logout(
    body: schemas.LogoutRequest,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    # DB에서 해당 리프레시 토큰 삭제 → 무효화
    deleted = (
        db.query(models.RefreshToken)
        .filter(
            models.RefreshToken.token == body.refresh_token,
            models.RefreshToken.user_id == current_user.user_id,
        )
        .first()
    )
    if not deleted:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="토큰을 찾을 수 없습니다.",
        )
    db.delete(deleted)
    db.commit()
    return schemas.MessageResponse(message="로그아웃 되었습니다.")


# ── POST /auth/refresh : 액세스 토큰 갱신 ────────────────────
@router.post(
    "/refresh",
    response_model=schemas.RefreshResponse,
    summary="액세스 토큰 갱신",
)
def refresh_token(body: schemas.RefreshRequest, db: Session = Depends(get_db)):
    # 1) JWT 자체 검증
    payload = decode_token(body.refresh_token)
    if payload is None or payload.get("type") != "refresh":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="유효하지 않거나 만료된 리프레시 토큰입니다.",
        )

    user_id: str = payload.get("sub")

    # 2) DB에 저장된 토큰인지 확인 (로그아웃된 토큰 차단)
    db_token = (
        db.query(models.RefreshToken)
        .filter(
            models.RefreshToken.token == body.refresh_token,
            models.RefreshToken.user_id == user_id,
        )
        .first()
    )
    if not db_token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="이미 무효화된 토큰입니다. 다시 로그인 해주세요.",
        )

    # 3) 새 액세스 토큰 발급
    new_access_token = create_access_token(data={"sub": user_id})
    return schemas.RefreshResponse(access_token=new_access_token)


# ── PUT /auth/password/change : 비밀번호 변경 (로그인 상태) ──
@router.put(
    "/password/change",
    response_model=schemas.MessageResponse,
    summary="비밀번호 변경 (로그인 상태)",
)
def change_password(
    body: schemas.PasswordChangeRequest,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    # 현재 비밀번호 검증
    if not verify_password(body.current_pwd, current_user.user_pwd):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="현재 비밀번호가 올바르지 않습니다.",
        )

    # 동일 비밀번호 방지
    if body.current_pwd == body.new_pwd:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="새 비밀번호가 현재 비밀번호와 동일합니다.",
        )

    # 비밀번호 업데이트
    current_user.user_pwd = hash_password(body.new_pwd)
    db.commit()

    return schemas.MessageResponse(message="비밀번호가 성공적으로 변경되었습니다.")
