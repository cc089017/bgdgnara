import bcrypt
from datetime import datetime, timedelta, timezone
from jose import JWTError, jwt

# ── 설정값 (실제 배포 시 환경변수로 분리 권장) ──────────────────
SECRET_KEY = "your-super-secret-key-change-in-production"  # 반드시 변경!
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30        # 액세스 토큰 유효 시간
REFRESH_TOKEN_EXPIRE_DAYS = 7           # 리프레시 토큰 유효 시간


# ── 비밀번호 해싱 (bcrypt 직접 사용) ───────────────────────────────
def hash_password(plain: str) -> str:
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(plain.encode('utf-8'), salt)
    return hashed.decode('utf-8')


def verify_password(plain: str, hashed: str) -> bool:
    return bcrypt.checkpw(plain.encode('utf-8'), hashed.encode('utf-8'))


# ── JWT 생성 ────────────────────────────────────────────────────
def create_access_token(data: dict) -> str:
    payload = data.copy()
    now = datetime.now(timezone.utc)
    expire = now + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    payload.update({"exp": expire, "iat": now, "type": "access"})
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


def create_refresh_token(data: dict) -> str:
    payload = data.copy()
    now = datetime.now(timezone.utc)
    expire = now + timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)
    payload.update({"exp": expire, "iat": now, "type": "refresh"})
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


# ── JWT 검증 ────────────────────────────────────────────────────
def decode_token(token: str) -> dict:
    """토큰 검증 후 payload 반환. 실패 시 None 반환."""
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError:
        return None
