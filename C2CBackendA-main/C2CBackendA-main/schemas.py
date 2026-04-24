from pydantic import BaseModel, Field
from typing import Optional


# ── 회원가입 요청 ──────────────────────────────────────────
class RegisterRequest(BaseModel):
    user_id: str = Field(..., max_length=20, example="hong123")
    user_pwd: str = Field(..., min_length=6, example="secret123")
    nickname: str = Field(..., max_length=20, example="홍길동")
    phone_num: str = Field(..., max_length=20, example="010-1234-5678")
    email: Optional[str] = Field(None, max_length=50, example="test@example.com")
    region: str = Field(..., max_length=20, example="서울 강남구")


# ── 회원가입 응답 ──────────────────────────────────────────
class RegisterResponse(BaseModel):
    message: str
    user_id: str


# ── 로그인 요청 / 응답 ─────────────────────────────────────────
class LoginRequest(BaseModel):
    user_id: str = Field(..., max_length=20, example="hong123")
    user_pwd: str = Field(..., min_length=6, example="secret123")


class LoginResponse(BaseModel):
    message: str
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


# ── 로그아웃 ───────────────────────────────────────────────────
class LogoutRequest(BaseModel):
    refresh_token: str = Field(..., description="저장된 리프레시 토큰")


class MessageResponse(BaseModel):
    message: str


# ── 액세스 토큰 갱신 ──────────────────────────────────────────
class RefreshRequest(BaseModel):
    refresh_token: str = Field(..., description="유효한 리프레시 토큰")


class RefreshResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"


# ── 비밀번호 변경 ─────────────────────────────────────────────
class PasswordChangeRequest(BaseModel):
    current_pwd: str = Field(..., min_length=6, example="oldpass123")
    new_pwd: str = Field(..., min_length=6, example="newpass456")


# ── 상품(Product) 관련 ─────────────────────────────────────────
class ProductBase(BaseModel):
    product_title: str = Field(..., max_length=50)
    product_body: Optional[str] = Field(None, max_length=100)
    product_price: int = Field(..., ge=0)
    category: str = Field(..., max_length=20)


class ProductCreate(ProductBase):
    pass


class ProductUpdate(BaseModel):
    product_title: Optional[str] = Field(None, max_length=50)
    product_body: Optional[str] = Field(None, max_length=100)
    product_price: Optional[int] = Field(None, ge=0)
    category: Optional[str] = Field(None, max_length=20)


class ProductStatusUpdate(BaseModel):
    status: str = Field(..., example="예약중")  # 판매중, 예약중, 판매완료


class ProductResponse(ProductBase):
    product_id: int
    user_id: str
    product_status: str
    thumbnail_url: Optional[str] = None  # 대표 이미지 URL (image_order 가장 낮은 이미지)

    class Config:
        from_attributes = True


# ── 상품 상세 (판매자 정보 + 전체 이미지 포함) ─────────────────
class ProductDetailResponse(ProductResponse):
    seller_nickname: str
    seller_region: str
    image_urls: list[str]  # 전체 이미지 URL 리스트 (image_order 순)


# ── 검색 결과 ──────────────────────────────────────────────────
class UserSimpleResponse(BaseModel):
    user_id: str
    nickname: str
    region: str

    class Config:
        from_attributes = True


class IntegratedSearchResponse(BaseModel):
    products: list[ProductResponse]
    users: list[UserSimpleResponse]


# ── 유저 프로필 관련 ───────────────────────────────────────────
class UserResponse(BaseModel):
    user_id: str
    nickname: str
    phone_num: str
    email: Optional[str]
    region: str

    class Config:
        from_attributes = True


class UserUpdate(BaseModel):
    nickname: Optional[str] = Field(None, max_length=20)
    phone_num: Optional[str] = Field(None, max_length=20)
    email: Optional[str] = Field(None, max_length=50)
    region: Optional[str] = Field(None, max_length=20)


