from sqlalchemy import Column, Integer, String, ForeignKey, LargeBinary, Boolean, DateTime
from datetime import datetime
from sqlalchemy.orm import relationship
from database import Base


class User(Base):
    __tablename__ = "users"

    user_id = Column(String(20), primary_key=True)
    user_pwd = Column(String(255), nullable=False)
    nickname = Column(String(20), nullable=False)
    phone_num = Column(String(20), nullable=False)
    email = Column(String(50), nullable=True)
    profile_img = Column(LargeBinary, nullable=True)
    region = Column(String(20), nullable=False)

    products = relationship("Product", back_populates="owner", cascade="all, delete-orphan")


class Product(Base):
    __tablename__ = "product"

    product_id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String(20), ForeignKey("users.user_id", ondelete="CASCADE"), nullable=False)
    product_status = Column(String(20), default="판매중")
    product_title = Column(String(50), nullable=False)
    product_body = Column(String(100), nullable=True)
    product_price = Column(Integer, nullable=False)
    category = Column(String(20), nullable=False, default="기타")

    owner = relationship("User", back_populates="products")
    images = relationship("ProductImage", back_populates="product", cascade="all, delete-orphan")

    @property
    def thumbnail_url(self):
        if not self.images:
            return None
        first = min(self.images, key=lambda img: img.image_order)
        return f"/products/{self.product_id}/images/{first.image_order}"


class ProductImage(Base):
    __tablename__ = "product_image"

    image_id = Column(Integer, primary_key=True, autoincrement=True)
    product_id = Column(Integer, ForeignKey("product.product_id", ondelete="CASCADE"), nullable=False)
    image_data = Column(LargeBinary, nullable=False)
    image_order = Column(Integer, default=0)  # 이미지 순서 (0이 대표 이미지)

    product = relationship("Product", back_populates="images")


class Admin(Base):
    __tablename__ = "admin"

    admin_id = Column(String(20), primary_key=True)
    admin_pwd = Column(String(255), nullable=False)
    admin_name = Column(String(20), nullable=False)
    is_super = Column(Boolean, default=False)  # 최고 관리자 여부


class RefreshToken(Base):
    """리프레시 토큰 저장 테이블 (로그아웃 시 삭제하여 무효화)"""
    __tablename__ = "refresh_tokens"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String(20), ForeignKey("users.user_id", ondelete="CASCADE"), nullable=False)
    token = Column(String(512), nullable=False, unique=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    expires_at = Column(DateTime, nullable=False)

    owner = relationship("User", backref="refresh_tokens")
