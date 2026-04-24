# CLAUDE.md — 프로젝트 협업 지침

## 작업 규칙

1. **대규모 코드 변경 시 사전 허가 필요**
   - 많은 양의 코드를 한번에 바꿔야 하는 상황이면 사용자 허가 후 진행

2. **더 나은 방향 제안 시 고지 후 승인**
   - 잘못된 방향이 있다면 "이러이러한 이유로 이런 방향이 더 좋다"고 고지하고 승인받고 진행

3. **기능별 모듈화**
   - 코드를 하나에 몰아넣지 말고 기능별로 모듈화

4. **Mock → Remote 점진적 교체**
   - 현재 백엔드 일부 연결 중 (인증 + 상품 목록/상세 = Phase 1 완료)
   - 아직 백엔드 미구현 기능(찜/등록/수정/검색/채팅/관리자 등)은 Mock 유지
   - Phase별로 `RemoteXxxRepository` 추가하며 ViewModel 생성자 교체

5. **로컬 테스트 방식: 실기기 + USB (고정)**
   - `BASE_URL = "http://localhost:8000/"` 유지 (에뮬레이터용 `10.0.2.2`로 바꾸지 말 것)
   - 테스트 순서:
     1. 백엔드: `cd C2CBackendA-main/C2CBackendA-main && uvicorn main:app --host 0.0.0.0 --port 8000 --reload`
     2. USB 연결 후: `adb reverse tcp:8000 tcp:8000` (USB 재연결 시마다 재실행 필요)
     3. Android Studio에서 앱 빌드·설치
   - WiFi 연결과 무관하게 USB 터널링으로만 동작

6. **api.md는 사용자가 직접 관리가 원칙**
   - 기본적으로 api.md 파일은 수정하지 말고, 변경이 필요하면 텍스트로만 알려줄 것
   - 단, 사용자가 명시적으로 "이번엔 네가 수정해라" 요청 시에만 수정 가능

---

## 기술 스택

### Android
- **UI**: Jetpack Compose, Navigation Compose
- **아키텍처**: MVVM + StateFlow
- **이미지**: Coil (AsyncImage), ActivityResultContracts (이미지 피커)
- **네트워크**: Retrofit + OkHttp + Gson Converter
- **JWT 토큰 저장**: DataStore (Preferences)

### 백엔드 (`C2CBackendA-main/C2CBackendA-main/`)
- **프레임워크**: Python + FastAPI
- **DB**: MySQL + SQLAlchemy ORM
- **인증**: JWT (access + refresh), bcrypt 해싱
- **파일 구조**: `main.py`, `database.py`, `models.py`, `schemas.py`, `core/security.py`, `routers/{auth,products,users,misc}.py`

**중요**: Pydantic 필드명(snake_case)과 Android DTO 필드명 매핑은 `@SerializedName`으로 처리. 도메인 모델은 camelCase 유지.

---

## 작업 요약 (2026-04-24 — 백엔드 연동 Phase 1)

### 범위
인증(로그인/회원가입/로그아웃/토큰갱신/비번변경/탈퇴) + 유저 프로필(조회/수정/공개프로필) + 상품(목록/상세/내상품/유저상품) 백엔드 연결.

### 백엔드 변경 (C2CBackendA-main)
- `models.py` — `Product.thumbnail_url` @property 추가 (대표 이미지 URL 자동 생성)
- `schemas.py` — `ProductResponse.thumbnail_url` + `ProductDetailResponse` 신규 (seller_nickname, seller_region, image_urls 포함)
- `routers/products.py`:
  - `GET /products/{product_id}` 신규 — 단일 상세 + 판매자 정보 + 이미지 URL
  - `GET /products/{product_id}/images` 신규 — 이미지 순서/URL 리스트
  - `GET /products/{product_id}/images/{image_order}` 신규 — 바이너리 서빙
  - `GET /products`, `GET /products/me` — `selectinload(images)` 추가 (N+1 방지)

### Android 신규 디렉토리
- `data/remote/` — Retrofit 인프라
  - `TokenStorage.kt` (DataStore), `AuthInterceptor.kt` (Bearer 자동주입), `RetrofitClient.kt` (싱글톤), `ApiCall.kt` (safeApiCall + FastAPI detail 파싱), `Mapper.kt` (DTO → Domain)
  - `api/AuthApi.kt`, `api/UserApi.kt`, `api/ProductApi.kt` (Retrofit 인터페이스)
  - `dto/AuthDto.kt`, `dto/UserDto.kt`, `dto/ProductDto.kt` (@SerializedName 매핑)
- `data/repository/remote/`
  - `RemoteAuthRepository`, `RemoteUserRepository`, `RemoteProductRepository` (미구현 기능은 Mock 위임)

### Android 수정
- `build.gradle.kts` + `libs.versions.toml` — Retrofit/OkHttp/DataStore 추가, `BuildConfig.BASE_URL = "http://localhost:8000/"`, `buildConfig = true`
- `AndroidManifest.xml` — INTERNET 권한, `usesCleartextTraffic="true"` (adb reverse로 localhost 접근)
- `MainActivity.kt` — `RetrofitClient.init(applicationContext)`
- `AuthModel.kt` — RegisterRequest에 `phoneNum`, `region` 추가
- `UserModel.kt` — UserProfile / UpdateProfileRequest에 `phoneNum` 추가
- `ProductModel.kt` — ProductDetail에 `thumbnailUrl`, `imageUrls` 추가 (기본값 있어서 Mock 호환)
- `MockData.kt` (Product) — `thumbnailUrl: String? = null` 추가
- ViewModel 생성자 교체: `AuthViewModel`, `HomeViewModel`, `ProductDetailViewModel`, `UserViewModel`, `ProductViewModel(mypage)`, `UserProfileViewModel` → Remote 사용
- `SignUpScreen.kt` — 전화번호/지역 입력 필드 추가
- `ProductCard.kt` — thumbnailUrl 있으면 Coil AsyncImage, 없으면 drawable fallback
- `ProductDetailScreen.kt` — ImageCarousel이 `imageUrls: List<String>` 기반 동작

### 실행 방법
1. 백엔드: `cd C2CBackendA-main/C2CBackendA-main && uvicorn main:app --host 0.0.0.0 --port 8000 --reload`
2. 실기기 USB 연결 후 `adb reverse tcp:8000 tcp:8000` (WiFi 독립, 재연결 시마다 재실행)
3. 앱 실행 → 회원가입 → 로그인 → 홈/상세 확인
4. Swagger(`http://localhost:8000/docs`)에서 상품/이미지 테스트 데이터 넣으면 앱에 바로 반영

### Phase 1 범위 밖 (Mock 유지)
찜(like/unlike/liked목록), 상품 등록/수정/삭제, 상품 상태 변경, 상품 이미지 업로드, 검색, 아바타 업로드, 채팅, 관리자, 배너

---

## 작업 요약 (2026-04-23 추가 3 — 채팅)

### 채팅 (Chat)
- `MockData.kt` — `ChatMessage`, `ChatRoom` 데이터 클래스 추가 + 목 채팅방/메시지 데이터
- `ChatViewModel.kt` — 채팅방 목록 + 마지막 메시지 조회
- `ChatRoomViewModel.kt` — 특정 채팅방 메시지 로드 + 로컬 메시지 전송 (factory 패턴)
- `ChatListScreen.kt` — 채팅방 목록 (아바타, 닉네임, 마지막 메시지, 안 읽은 수 배지), 하단 네비게이션 바 포함
- `ChatRoomScreen.kt` — 채팅방 (상품 정보 바, 말풍선 메시지 목록, 이미지 첨부 + 전송 입력창)
- `BottomNavBar.kt` — CHAT 탭 라우트 `Route.ChatList.path` 로 수정
- `ProductDetailScreen.kt` — "채팅하기" 버튼 `onChatClick` 콜백 연결

---

## 작업 요약 (2026-04-23 추가 2)

### 검색 (Search)
- `ProductRepository.kt` — `searchProducts(query: String)` 메서드 추가
- `MockProductRepository.kt` — 상품명 기준 contains 필터 구현
- `SearchViewModel.kt` — debounce(300ms) + distinctUntilChanged 실시간 검색
- `SearchScreen.kt` — 검색창 + 결과 그리드 + 빈 결과/힌트 상태 처리, 하단 네비게이션 바 포함
- `BottomNavBar.kt` — SEARCH 탭 라우트 `Route.Search.path` 로 수정 (기존 오류 수정)

### 관리자 (Admin)
- `AdminViewModel.kt` — 유저 검색·정지, 상품 검색·필터·삭제, 배너 이미지 URI 관리
- `AdminScreen.kt` — TabRow 3탭 구성
  - 유저 관리: 닉네임·지역 검색 + LazyColumn + 정지/해제 버튼
  - 상품 관리: 상품명 검색 + 상태 FilterChip + LazyColumn + 삭제 AlertDialog
  - 배너 관리: 현재 배너 목록 + 갤러리 다중선택 + 미리보기 + 업로드 버튼 (POST /admin/upload 예정)
- 진입점: 홈 화면 "번개당근나라" 텍스트 클릭 → AdminScreen (추후 admin_id 권한 체크 예정)

---

## 작업 요약 (2026-04-21)

### 인증 (Auth)
- `AuthModel.kt` — LoginRequest, RegisterRequest, AuthToken, AuthResult sealed class
- `AuthRepository.kt` — login, register, logout, refreshToken 인터페이스
- `MockAuthRepository.kt` — Mock 구현
- `AuthViewModel.kt` — 로그인/회원가입 상태 관리
- `LoginScreen.kt`, `SignUpScreen.kt` — 로그인·회원가입 화면 UI

### 사용자/프로필 (User)
- `UserModel.kt` — UserProfile (`id: String`), UpdateProfileRequest, ChangePasswordRequest, UserResult sealed class
- `UserRepository.kt` — getMyProfile, getPublicProfile(userId: String), updateMyProfile, uploadAvatar, changePassword, deleteAccount 인터페이스
- `MockUserRepository.kt` — Mock 구현 (sellers 목록 기반 공개 프로필 반환)
- `UserViewModel.kt` — 프로필 조회/수정/삭제 상태 관리
- `ProfileEditScreen.kt` — 닉네임·지역 수정, 아바타 교체, 비밀번호 변경, 회원탈퇴 AlertDialog
- `UserProfileViewModel.kt` — 타 유저 공개 프로필 + 판매 상품 목록 동시 로드
- `UserProfileScreen.kt` — 타 유저 프로필 (아바타, 닉네임, 지역, 판매 상품 그리드)

### 상품 (Product)
- `ProductModel.kt` — RegisterProductRequest, ProductDetail 데이터 클래스
- `ProductRepository.kt` — 전체 상품 API 인터페이스 (CRUD + 좋아요 + 상태변경 + getProductsByUser)
- `MockProductRepository.kt` — Mock 구현 (likedIds 내부 상태, getProductsByUser는 sellerId로 필터)
- `HomeViewModel.kt` — 홈 상품 목록 + toggleLike
- `ProductViewModel.kt` (mypage) — 내 상품 목록 조회
- `WishlistViewModel.kt` — 찜한 상품 목록 + unlike
- `ProductRegisterViewModel.kt` — 상품 등록
- `ProductDetailViewModel.kt` — 상세 조회 + 좋아요 + 삭제 + 상태변경
- `ProductEditViewModel.kt` — 상품 수정 (기존 정보 로드 후 저장)
- `ProductDetailScreen.kt` — 번개장터 스타일 상세 페이지 (이미지 캐러셀, 판매자, 상태배지, 찜/채팅/구매 하단바, MoreVert 메뉴)
- `ProductEditScreen.kt` — 상품 수정 폼 화면 (상품명/가격/카테고리/설명)
- `ProductRegisterScreen.kt` — 상품 등록 폼
- `AutoPriceScreen.kt` — 자동 시세 조회 화면

### 공통
- `ProductCard.kt` — 찜 버튼(하트) 포함 카드 컴포넌트
- `BottomNavBar.kt` — 하단 네비게이션 바
- `AppNavigation.kt` — 전체 라우트 정의 및 화면 연결

### 해결한 버그
- 삼성 제스처 네비게이션 바 겹침 → `navigationBarsPadding()` + `WindowInsets` 동적 높이 계산으로 수정

---

## 전체 디렉토리 구조

```
app/src/main/
├── AndroidManifest.xml                         # INTERNET 권한 + cleartext 허용
└── java/com/example/bgjz_app/
    │
    ├── MainActivity.kt                         # 앱 진입점, RetrofitClient.init() + AppNavigation 호스팅
    │
    ├── data/
    │   ├── mock/
    │   │   └── MockData.kt                     # 더미 상품·유저·배너·채팅 데이터
    │   │
    │   ├── model/                              # 앱 도메인 모델 (UI가 직접 사용)
    │   │   ├── AuthModel.kt                    # LoginRequest, RegisterRequest(phoneNum/region 포함), AuthToken, AuthResult
    │   │   ├── UserModel.kt                    # UserProfile(phoneNum), UpdateProfileRequest, ChangePasswordRequest, UserResult
    │   │   └── ProductModel.kt                 # RegisterProductRequest, ProductDetail(thumbnailUrl, imageUrls)
    │   │
    │   ├── remote/                             # Retrofit 네트워크 인프라 (Phase 1)
    │   │   ├── TokenStorage.kt                 # DataStore 기반 JWT 저장/읽기/삭제
    │   │   ├── AuthInterceptor.kt              # Authorization: Bearer 자동 주입
    │   │   ├── RetrofitClient.kt               # Retrofit/OkHttp 싱글톤
    │   │   ├── ApiCall.kt                      # safeApiCall + ApiResult + FastAPI detail 파싱
    │   │   ├── Mapper.kt                       # DTO → Domain 변환 + 절대 URL 생성
    │   │   ├── api/
    │   │   │   ├── AuthApi.kt                  # /auth/{login,register,logout,refresh,password/change}
    │   │   │   ├── UserApi.kt                  # /users/{me, {user_id}, {user_id}/products}
    │   │   │   └── ProductApi.kt               # /products, /products/me, /products/{id}
    │   │   └── dto/                            # 서버와 주고받는 JSON 모양 (@SerializedName)
    │   │       ├── AuthDto.kt                  # LoginRequestDto, LoginResponseDto, RegisterRequestDto 등
    │   │       ├── UserDto.kt                  # UserResponseDto, UserUpdateDto
    │   │       └── ProductDto.kt               # ProductResponseDto, ProductDetailResponseDto
    │   │
    │   └── repository/
    │       ├── AuthRepository.kt               # 인터페이스
    │       ├── UserRepository.kt               # 인터페이스
    │       ├── ProductRepository.kt            # 인터페이스
    │       │
    │       ├── mock/                           # Mock 구현 — 백엔드 미구현 기능들이 아직 씀
    │       │   ├── MockAuthRepository.kt
    │       │   ├── MockUserRepository.kt
    │       │   └── MockProductRepository.kt
    │       │
    │       └── remote/                         # Retrofit 구현 — Phase 1에서 활성화
    │           ├── RemoteAuthRepository.kt     # login/register/logout/refresh/passwordChange 전체 연결
    │           ├── RemoteUserRepository.kt     # 아바타 업로드 제외 전체 연결
    │           └── RemoteProductRepository.kt  # 목록/상세/내상품/유저상품 연결, 나머지는 Mock 위임
    │
    └── ui/
        ├── components/
        │   ├── BottomNavBar.kt                 # 하단 탭바 (홈/검색/등록/채팅/마이)
        │   └── ProductCard.kt                  # 상품 카드 (thumbnailUrl 있으면 AsyncImage, 없으면 drawable)
        │
        ├── navigation/
        │   └── AppNavigation.kt                # Route sealed class + NavHost 전체 라우트 정의
        │
        ├── screens/
        │   ├── splash/SplashScreen.kt
        │   ├── onboarding/OnboardingScreen.kt
        │   │
        │   ├── auth/
        │   │   ├── AuthViewModel.kt            # RemoteAuthRepository 사용, register(phoneNum/region 포함)
        │   │   ├── LoginScreen.kt
        │   │   └── SignUpScreen.kt             # 전화번호/지역 필드 추가됨
        │   │
        │   ├── chat/                           # Mock 유지 (Phase 2 대상)
        │   │   ├── ChatViewModel.kt
        │   │   ├── ChatRoomViewModel.kt
        │   │   ├── ChatListScreen.kt
        │   │   └── ChatRoomScreen.kt
        │   │
        │   ├── admin/                          # Mock 유지
        │   │   ├── AdminViewModel.kt
        │   │   └── AdminScreen.kt
        │   │
        │   ├── home/
        │   │   ├── HomeViewModel.kt            # RemoteProductRepository 사용
        │   │   └── HomeScreen.kt
        │   │
        │   ├── wishlist/                       # Mock 유지 (찜 백엔드 미구현)
        │   │   ├── WishlistViewModel.kt
        │   │   └── WishlistScreen.kt
        │   │
        │   ├── mypage/
        │   │   ├── UserViewModel.kt            # RemoteUserRepository 사용
        │   │   ├── ProductViewModel.kt         # RemoteProductRepository 사용 (내 상품)
        │   │   ├── MyPageScreen.kt
        │   │   └── ProfileEditScreen.kt
        │   │
        │   ├── userprofile/
        │   │   ├── UserProfileViewModel.kt     # Remote* 사용
        │   │   └── UserProfileScreen.kt
        │   │
        │   ├── search/                         # Mock 유지 (검색 백엔드 미구현)
        │   │   ├── SearchViewModel.kt
        │   │   └── SearchScreen.kt
        │   │
        │   └── product/
        │       ├── ProductDetailViewModel.kt   # RemoteProductRepository 사용
        │       ├── ProductDetailScreen.kt      # ImageCarousel이 imageUrls 기반
        │       ├── ProductEditViewModel.kt     # Mock 유지
        │       ├── ProductEditScreen.kt
        │       ├── ProductRegisterViewModel.kt # Mock 유지
        │       ├── ProductRegisterScreen.kt
        │       └── AutoPriceScreen.kt
        │
        └── theme/
            ├── Color.kt
            ├── Theme.kt
            └── Type.kt

C2CBackendA-main/C2CBackendA-main/              # FastAPI 백엔드
├── main.py                                     # app 생성, /auth/register, 헬스체크
├── database.py                                 # SQLAlchemy 엔진 + get_db 의존성
├── models.py                                   # User, Product(+thumbnail_url property), ProductImage, Admin, RefreshToken
├── schemas.py                                  # Pydantic DTO (ProductDetailResponse 포함)
├── core/
│   └── security.py                             # bcrypt, JWT 발급/검증
└── routers/
    ├── auth.py                                 # login/logout/refresh/password-change
    ├── products.py                             # 목록/단일상세/이미지서빙/등록/수정/삭제/검색
    ├── users.py                                # 프로필 조회/수정/탈퇴
    └── misc.py                                 # 카테고리/지역/공지 (일부 mock)
```

---

## 백엔드 연결 상태

| 기능 | 상태 | Repository |
|---|---|---|
| 로그인/회원가입/로그아웃/토큰갱신/비번변경 | ✅ Remote | `RemoteAuthRepository` |
| 내 프로필 조회/수정/탈퇴, 타 유저 프로필 | ✅ Remote | `RemoteUserRepository` |
| 상품 목록/단일 상세/내 상품/유저별 상품 | ✅ Remote | `RemoteProductRepository` |
| 상품 이미지 서빙 (GET) | ✅ Remote | `/products/{id}/images/{order}` 바이너리 |
| 아바타 업로드 | ⬜ Phase 2 | 백엔드 엔드포인트 미구현 |
| 상품 등록/수정/삭제/상태변경/이미지업로드 | 🔧 Mock | UI만 있음, Remote 미교체 |
| 찜 (like/unlike/getLiked) | 🔧 Mock | 백엔드 테이블 미구현 |
| 검색 | 🔧 Mock | 백엔드 `/search` 있지만 Remote 미교체 |
| 채팅 | 🔧 Mock | 백엔드 전체 미구현 |
| 관리자 (유저정지/상품관리/배너) | 🔧 Mock | 백엔드 전체 미구현 |

### 새 기능을 Remote로 승격할 때
1. 백엔드 엔드포인트 확인 후 `data/remote/api/XxxApi.kt`에 메서드 추가
2. 필요하면 `dto/` 확장, `Mapper.kt`에 toDomain() 매퍼 추가
3. `RemoteXxxRepository`에서 Mock 위임 → 실제 API 호출로 교체
4. Data 레이어 외 수정 불필요 (ViewModel/UI는 그대로)
