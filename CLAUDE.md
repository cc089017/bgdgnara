# CLAUDE.md — 프로젝트 협업 지침

## 작업 규칙

1. **대규모 코드 변경 시 사전 허가 필요**
   - 많은 양의 코드를 한번에 바꿔야 하는 상황이면 사용자 허가 후 진행

2. **더 나은 방향 제안 시 고지 후 승인**
   - 잘못된 방향이 있다면 "이러이러한 이유로 이런 방향이 더 좋다"고 고지하고 승인받고 진행

3. **기능별 모듈화**
   - 코드를 하나에 몰아넣지 말고 기능별로 모듈화

4. **Mock → Remote 점진적 교체**
   - Phase 1 완료: 인증 + 유저 프로필 + 상품 CRUD/검색/이미지업로드/상태변경
   - Phase 2 완료: 배너 + 관리자(유저 권한 토글/상품 관리/배너 등록·삭제)
   - 아직 백엔드 미구현 기능(찜/채팅/아바타 업로드/리뷰)은 Mock 유지
   - Phase별로 `RemoteXxxRepository` 추가하며 ViewModel 생성자 교체

5. **로컬 테스트 방식: 실기기 + USB (고정)**
   - `BASE_URL = "http://localhost:8000/"` 유지 (에뮬레이터용 `10.0.2.2`로 바꾸지 말 것)
   - 테스트 순서:
     1. 백엔드: `  - 신규: cd C2CBackendA-main && python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload`
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
- **파일 구조**: `main.py`, `database.py`, `models.py`, `schemas.py`, `core/security.py`, `routers/{auth,products,users,misc,banners}.py`

**중요**: Pydantic 필드명(snake_case)과 Android DTO 필드명 매핑은 `@SerializedName`으로 처리. 도메인 모델은 camelCase 유지.

---

## 작업 요약 (2026-04-26 — 백엔드 연동 Phase 2: 배너 + 관리자)

### 범위
1. 홈 화면 배너를 `GET /banners` 백엔드 데이터로 교체
2. 관리자 화면(유저/상품/배너 관리) 전체를 Mock에서 백엔드로 교체
3. AdminScreen 진입 시 본인이 관리자가 아니면 **"접근 권한이 없습니다"** 즉시 차단

### 백엔드 변경
없음. **옵션 A — 백엔드 무수정 원칙**으로 결정. 백엔드 모델/엔드포인트는 그대로 두고 앱만 수정.

### Android 신규
**배너**
- `data/model/BannerModel.kt` — `Banner(id, imageUrl, linkUrl, title)` 도메인 모델
- `data/remote/dto/BannerDto.kt` — `BannerResponseDto`, `BannerCreateDto`
- `data/remote/api/BannerApi.kt` — `GET /banners`, `POST /banners`(관리자), `DELETE /banners/{id}`(관리자)
- `data/repository/BannerRepository.kt` — 인터페이스
- `data/repository/remote/RemoteBannerRepository.kt` — 실제 구현

### Android 수정
**공용**
- `data/remote/dto/UserDto.kt` — `UserResponseDto`에 `is_admin` 필드 추가 (백엔드는 보냈으나 누락돼 있던 매핑)
- `data/model/UserModel.kt` — `UserProfile.isAdmin` 추가
- `data/remote/Mapper.kt` — `absoluteUrl()`이 `http(s)://` 시작 URL은 통과시키도록 보강 (배너는 외부 CDN URL 가능), `BannerResponseDto.toDomain()` 추가, `UserResponseDto.toDomain()`에 isAdmin 매핑
- `data/remote/RetrofitClient.kt` — `bannerApi` 노출
- `data/remote/api/UserApi.kt` — `getAllUsers()`, `toggleUserAdmin(userId)` 추가
- `data/repository/UserRepository.kt` + Mock + Remote — 위 두 메서드 인터페이스/구현 추가

**홈 배너**
- `HomeViewModel.kt` — `BannerRepository` 주입, `HomeUiState.banners` 추가, `loadBanners()` (실패 시 화면 에러로 안 띄우고 무시)
- `HomeScreen.kt` — `BannerCarousel(banners: List<Banner>)`로 교체, Coil `AsyncImage`로 URL 로드. **배너 0개일 때는 `PlaceholderBanner()` (drawable + "번개당근나라" 타이틀) 표시**

**관리자 화면 (전면 재작성)**
- `AdminViewModel.kt`:
  - 의존성: `UserRepository`, `ProductRepository`, `BannerRepository` (모두 Remote)
  - `init`에서 `getMyProfile()` 호출 → `isAdmin=false`면 `accessDenied=true`로 즉시 차단 (데이터 로드 안 함)
  - 본인 권한 토글 차단 (클라/백엔드 둘 다)
  - 상품 검색은 `searchProducts()` 백엔드 호출, 상태 필터(`판매중/예약중/판매완료`)는 클라이언트에서 (백엔드에 status 쿼리 없음)
  - 메시지/에러 → Snackbar용 1회성 상태 (`message`, `error`, `consumeMessage/consumeError`)
- `AdminScreen.kt`:
  - `accessDenied=true` → 자물쇠 아이콘 + "접근 권한이 없습니다" 화면만 표시
  - 유저 탭: 매너점수 제거(백엔드에 없음), 닉네임/아이디/지역 클라 검색, 관리자 배지 표시, **"관리자 지정 ↔ 권한 해제"** 토글(AlertDialog 확인), 본인은 비활성
  - 상품 탭: 백엔드 검색, 상태 FilterChip 필터, 삭제 시 `DELETE /products/{id}` (백엔드가 관리자 권한 통과시킴)
  - 배너 탭: 갤러리 launcher 제거, **이미지 URL/타이틀/링크URL 텍스트 입력 폼 + 미리보기 + 등록 버튼**, 기존 배너 카드에 삭제 버튼

### UX 결정 (옵션 A — 모두 백엔드 무수정)
| 탭 | 기존 (Mock) | 신규 (Remote) | 이유 |
|---|---|---|---|
| 유저 관리 | "정지/해제" 버튼 | "관리자 지정/권한 해제" 토글 | 백엔드에 `is_suspended` 컬럼 자체가 없음 → `PATCH /users/{id}/admin`로 대체 |
| 상품 관리 | 클라 필터링 | 검색은 백엔드, 상태 필터는 클라 | 백엔드 `GET /products`에 status 쿼리 없음 |
| 배너 관리 | 갤러리 다중선택 + "업로드" | URL 입력 폼 + 등록 + 삭제 | 백엔드는 `image_url` 텍스트만 받음, 바이너리 업로드 엔드포인트/정적 파일 서빙 모두 없음 |

### 테스트 방법
1. 백엔드 실행 + `adb reverse tcp:8000 tcp:8000`
2. **관리자 계정 만들기**: `add_admin_column.py` 또는 DB에서 `UPDATE users SET is_admin = 1 WHERE user_id = 'xxx';`
3. Swagger에서 `POST /banners`로 배너 시드(외부 URL 사용, 예: `https://picsum.photos/800/400?random=1`)
4. 앱 → 홈 → 배너 슬라이드 표시 확인 (시드 없으면 placeholder 1장)
5. 홈 → "번개당근나라" 텍스트 클릭 → AdminScreen
   - 비관리자 계정: "접근 권한이 없습니다" 화면
   - 관리자 계정: 3탭 정상 동작

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

### Phase 1 범위 밖 (작성 시점 기준 — 일부는 이후 Phase에서 완료됨)
찜(like/unlike/liked목록), 상품 등록/수정/삭제, 상품 상태 변경, 상품 이미지 업로드, 검색, 아바타 업로드, 채팅, 관리자, 배너
> ※ 이후 작업으로 상품 CRUD/상태/이미지/검색은 Remote 완료, 관리자/배너는 Phase 2에서 완료. 현재 잔존 Mock: 찜/채팅/아바타 업로드

---

## 인프라 (보안 / 빌드 설정)

### 백엔드 연결 준비
- `res/raw/server.crt` — 서버 SSL 인증서
- `res/xml/network_security_config.xml` — `bgdgnara.duckdns.org` 도메인 SSL 핀닝 설정 (시스템 레벨)
- `AndroidManifest.xml` — `android:networkSecurityConfig`, `INTERNET` 권한 추가
- `data/remote/RetrofitClient.kt` — OkHttp CertificatePinner + Retrofit 싱글턴 (앱 레벨 SSL 핀닝)
- `libs.versions.toml`, `build.gradle.kts` — OkHttp 4.12.0, Retrofit 2.11.0 의존성 추가

### 루팅 탐지 (Native / JNI)
- `build.gradle.kts` — NDK/CMake 설정 추가 (CMake 4.1.2, C++17)
- `cpp/CMakeLists.txt` — `root_detector` SHARED 라이브러리 정의, log 링크
- `cpp/root_detector.cpp` — C++ 네이티브 구현
  - `[AB]` `__system_property_get()`으로 ro.build.tags, ro.build.fingerprint, ro.secure, ro.adb.secure 검사
  - `[PB]` `stat()`으로 su 바이너리 13개 경로 + 루트 파일 6개 경로 존재 여부 검사
- `security/RootDetector.kt` — Kotlin 래퍼 (`object`)
  - `checkBuildAttributes()`, `checkPaths()` → 네이티브 호출
  - `checkPackages(context)` → Kotlin에서 PackageManager로 처리 (com.topjohnwu.magisk)
  - `checkAll(context)` — 세 가지 결과 종합

### 난독화 (ProGuard/R8)
- `build.gradle.kts` — release 빌드에 `isMinifyEnabled = true`, `isShrinkResources = true` 적용
- `proguard-rules.pro` — 규칙 설정
  - `RootDetector` keep — JNI 함수명 보존 (.so 연결 유지)
  - `data.model.**` keep — Gson JSON 역직렬화 필드명 보존
  - Retrofit / OkHttp / Kotlin / Compose dontwarn 처리
- release APK는 `app/build/outputs/apk/release/app-release.apk` 에 생성
- .so 파일은 난독화 미적용 — Ghidra/IDA 분석 연습용으로 유지

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
├── AndroidManifest.xml
├── cpp/
│   ├── CMakeLists.txt                          # NDK 빌드 설정 (root_detector SHARED 라이브러리)
│   └── root_detector.cpp                       # 루팅 탐지 네이티브 구현 (AB + PB)
├── res/
│   ├── raw/server.crt                          # 서버 SSL 인증서
│   └── xml/network_security_config.xml         # bgdgnara.duckdns.org SSL 핀닝 설정
└── java/com/example/bgjz_app/
    │
    ├── MainActivity.kt                         # 앱 진입점, AppNavigation 호스팅
    │
    ├── data/
    │   ├── mock/
    │   │   └── MockData.kt                     # 더미 상품·유저·배너 데이터 (Product, Banner 등 data class 포함)
    │   │
    │   ├── model/                              # 앱 도메인 모델 (UI가 직접 사용)
    │   │   ├── AuthModel.kt                    # LoginRequest, RegisterRequest(phoneNum/region 포함), AuthToken, AuthResult
    │   │   ├── UserModel.kt                    # UserProfile(phoneNum, isAdmin), UpdateProfileRequest, ChangePasswordRequest, UserResult
    │   │   ├── ProductModel.kt                 # RegisterProductRequest, ProductDetail(thumbnailUrl, imageUrls)
    │   │   └── BannerModel.kt                  # Banner(id, imageUrl, linkUrl, title)
    │   │
    │   ├── remote/                             # Retrofit 네트워크 인프라
    │   │   ├── TokenStorage.kt                 # DataStore 기반 JWT 저장/읽기/삭제
    │   │   ├── AuthInterceptor.kt              # Authorization: Bearer 자동 주입
    │   │   ├── RetrofitClient.kt               # Retrofit/OkHttp 싱글톤 (authApi/userApi/productApi/bannerApi)
    │   │   ├── ApiCall.kt                      # safeApiCall + ApiResult + FastAPI detail 파싱
    │   │   ├── Mapper.kt                       # DTO → Domain 변환 + 절대 URL 생성 (http(s):// 통과)
    │   │   ├── api/
    │   │   │   ├── AuthApi.kt                  # /auth/{login,register,logout,refresh,password/change}
    │   │   │   ├── UserApi.kt                  # /users/{me, {user_id}, {user_id}/products, getAllUsers, toggleUserAdmin}
    │   │   │   ├── ProductApi.kt               # /products, /products/me, /products/{id}, 검색/등록/수정/삭제/이미지/상태
    │   │   │   └── BannerApi.kt                # GET/POST/DELETE /banners
    │   │   └── dto/                            # 서버와 주고받는 JSON 모양 (@SerializedName)
    │   │       ├── AuthDto.kt                  # LoginRequestDto, LoginResponseDto, RegisterRequestDto 등
    │   │       ├── UserDto.kt                  # UserResponseDto(is_admin 포함), UserUpdateDto
    │   │       ├── ProductDto.kt               # ProductResponseDto, ProductDetailResponseDto, ProductCreate/Update/StatusUpdate
    │   │       └── BannerDto.kt                # BannerResponseDto, BannerCreateDto
    │   │
    │   └── repository/
    │       ├── AuthRepository.kt               # 인증 API 인터페이스 (login, register, logout, refreshToken)
    │       ├── UserRepository.kt               # 유저 API (getMyProfile, ..., 관리자: getAllUsers, toggleUserAdmin)
    │       ├── ProductRepository.kt            # 상품 API (CRUD + 좋아요 + 상태변경 + 검색 + getProductsByUser)
    │       ├── BannerRepository.kt             # 배너 API (getActiveBanners, createBanner, deleteBanner)
    │       │
    │       ├── mock/                           # Mock 구현 — 백엔드 미구현 기능들이 아직 씀
    │       │   ├── MockAuthRepository.kt
    │       │   ├── MockUserRepository.kt       # 관리자 메서드는 stub (Error 반환)
    │       │   └── MockProductRepository.kt
    │       │
    │       └── remote/                         # Retrofit 구현
    │           ├── RemoteAuthRepository.kt     # login/register/logout/refresh/passwordChange 전체 연결
    │           ├── RemoteUserRepository.kt     # 아바타 업로드 제외 전체 + 관리자(getAllUsers/toggleUserAdmin) 연결
    │           ├── RemoteProductRepository.kt  # 목록/상세/내상품/유저상품/검색/등록/수정/삭제/상태변경/이미지업로드 연결, 찜만 Mock 위임
    │           └── RemoteBannerRepository.kt   # 조회/등록/삭제 전체 연결
    │
    ├── security/
    │   └── RootDetector.kt                     # 루팅 탐지 Kotlin 래퍼 (네이티브 호출 + 패키지 검사)
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
        │   ├── admin/                          # Remote 사용 (Phase 2)
        │   │   ├── AdminViewModel.kt           # 진입 시 isAdmin 체크 → 비관리자 차단, 유저/상품/배너 관리
        │   │   └── AdminScreen.kt              # 권한 차단 화면 + 3탭 (유저/상품/배너)
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
        │   ├── search/                         # Remote 사용 (RemoteProductRepository.searchProducts → /products?search=)
        │   │   ├── SearchViewModel.kt          # 통합 검색(/search)으로 교체는 Phase 3 예정
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
├── main.py                                     # app 생성, /auth/register, 헬스체크, banners 라우터 등록
├── database.py                                 # SQLAlchemy 엔진 + get_db 의존성
├── models.py                                   # User(is_admin), Product(+thumbnail_url property), ProductImage, Admin, RefreshToken, Banner
├── schemas.py                                  # Pydantic DTO (ProductDetailResponse, BannerResponse/Create 포함)
├── core/
│   └── security.py                             # bcrypt, JWT 발급/검증
└── routers/
    ├── auth.py                                 # login/logout/refresh/password-change
    ├── products.py                             # 목록/단일상세/이미지서빙/등록/수정/삭제/검색/related
    ├── users.py                                # 프로필 조회/수정/탈퇴, /users(전체조회·관리자), PATCH /users/{id}/admin
    ├── misc.py                                 # 카테고리/지역/공지 (일부 mock)
    └── banners.py                              # GET (Public) / POST·DELETE (관리자)
```

---

## 백엔드 연결 상태

| 기능 | 상태 | Repository |
|---|---|---|
| 로그인/회원가입/로그아웃/토큰갱신/비번변경 | ✅ Remote | `RemoteAuthRepository` |
| 내 프로필 조회/수정/탈퇴, 타 유저 프로필 | ✅ Remote | `RemoteUserRepository` |
| 상품 목록/단일 상세/내 상품/유저별 상품 | ✅ Remote | `RemoteProductRepository` |
| 상품 이미지 서빙 (GET) | ✅ Remote | `/products/{id}/images/{order}` 바이너리 |
| 상품 등록/수정/삭제/상태변경/이미지업로드 | ✅ Remote | `RemoteProductRepository` |
| 상품명 검색 (Phase 3에서 통합 검색으로 교체 예정) | ✅ Remote | `/products?search=` |
| 배너 조회 (홈) | ✅ Remote | `RemoteBannerRepository.getActiveBanners` |
| 관리자: 유저 권한 토글 | ✅ Remote | `RemoteUserRepository.toggleUserAdmin` |
| 관리자: 전체 유저 조회 | ✅ Remote | `RemoteUserRepository.getAllUsers` |
| 관리자: 상품 삭제 (타인 상품) | ✅ Remote | `RemoteProductRepository.deleteProduct` |
| 관리자: 배너 등록/삭제 (URL 입력) | ✅ Remote | `RemoteBannerRepository.createBanner/deleteBanner` |
| 아바타 업로드 | ⬜ 미구현 | 백엔드 엔드포인트 미구현 |
| 찜 (like/unlike/getLiked) | 🔧 Mock | 백엔드 테이블 미구현 |
| 채팅 | 🔧 Mock | 백엔드 전체 미구현 |
| 통합 검색 `/search` (상품 + 유저) | ⬜ 미연결 | 백엔드 있음, Phase 3 대상 |
| 연관 상품 `/products/{id}/related` | ⬜ 미연결 | 백엔드 있음, Phase 3 대상 |
| 유저 정지 (is_suspended) | ❌ 컬럼 자체 없음 | 관리자 권한 토글로 대체 |
| 배너 이미지 바이너리 업로드 | ❌ 엔드포인트 없음 | URL 입력 방식 사용 |

### 새 기능을 Remote로 승격할 때
1. 백엔드 엔드포인트 확인 후 `data/remote/api/XxxApi.kt`에 메서드 추가
2. 필요하면 `dto/` 확장, `Mapper.kt`에 toDomain() 매퍼 추가
3. `RemoteXxxRepository`에서 Mock 위임 → 실제 API 호출로 교체
4. Data 레이어 외 수정 불필요 (ViewModel/UI는 그대로)
