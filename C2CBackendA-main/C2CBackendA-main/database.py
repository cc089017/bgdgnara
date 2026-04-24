from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# 실제 root 비밀번호를 넣으세요.
SQLALCHEMY_DATABASE_URL = "mysql+pymysql://root:1234@localhost:3306/secondhand_platform"

engine = create_engine(SQLALCHEMY_DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# DB 세션 의존성 (API 호출마다 세션을 열고 닫음)
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
