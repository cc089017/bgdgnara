# CLAUDE.md — 프로젝트 협업 지침

## 작업 규칙

1. **대규모 코드 변경 시 사전 허가 필요**
   - 많은 양의 코드를 한번에 바꿔야 하는 상황이면 사용자 허가 후 진행

2. **더 나은 방향 제안 시 고지 후 승인**
   - 잘못된 방향이 있다면 "이러이러한 이유로 이런 방향이 더 좋다"고 고지하고 승인받고 진행

3. **기능별 모듈화**
   - 코드를 하나에 몰아넣지 말고 기능별로 모듈화

4. **Mock 데이터 기반**
   - 현재는 백엔드 없이 Mock 데이터로 UI 동작
   - 나중에 백엔드 연결 시 Data 레이어만 교체하는 구조로 설계

5. **api.md는 사용자가 직접 관리**
   - api.md 파일은 절대 수정하지 말 것
   - 변경이 필요하면 텍스트로만 알려줄 것

---

## 기술 스택

- **Android**: Jetpack Compose, MVVM, Navigation Compose, StateFlow
- **이미지**: Coil (AsyncImage), ActivityResultContracts (이미지 피커)
- **백엔드 (예정)**: Python + FastAPI, Retrofit
  - Pydantic 필드명과 Android data class 필드명 일치시킬 것

---

## 작업 요약 (2026-04-23)

### 타 유저 프로필 (UserProfile)
- `UserProfileScreen.kt` — 타 유저 프로필 화면 (아바타, 닉네임, 지역, 판매 상품 그리드)
- `UserProfileViewModel.kt` — 타 유저 프로필 + 상품 목록 로드 (`getPublicProfile`, `getProductsByUser`)
- `UserRepository.kt` — `getPublicProfile(userId)` 인터페이스 추가
- `ProductRepository.kt` — `getProductsByUser(userId)` 인터페이스 추가
- `MockUserRepository.kt`, `MockProductRepository.kt` — 위 메서드 Mock 구현 추가

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
- `UserModel.kt` — UserProfile, UpdateProfileRequest, ChangePasswordRequest, UserResult sealed class
- `UserRepository.kt` — getMyProfile, updateMyProfile, uploadAvatar, changePassword, deleteAccount 인터페이스
- `MockUserRepository.kt` — Mock 구현
- `UserViewModel.kt` — 프로필 조회/수정/삭제 상태 관리
- `ProfileEditScreen.kt` — 닉네임·지역 수정, 아바타 교체, 비밀번호 변경, 회원탈퇴 AlertDialog

### 상품 (Product)
- `ProductModel.kt` — RegisterProductRequest, ProductDetail 데이터 클래스
- `ProductRepository.kt` — 전체 상품 API 인터페이스 (CRUD + 좋아요 + 상태변경)
- `MockProductRepository.kt` — Mock 구현 (likedIds 내부 상태 관리)
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
    │   ├── model/                              # 네트워크 요청/응답 데이터 클래스 (Retrofit 연결 시 그대로 사용)
    │   │   ├── AuthModel.kt                    # LoginRequest, RegisterRequest, AuthToken, AuthResult
    │   │   ├── UserModel.kt                    # UserProfile, UpdateProfileRequest, ChangePasswordRequest, UserResult
    │   │   └── ProductModel.kt                 # RegisterProductRequest, ProductDetail
    │   │
    │   ├── remote/
    │   │   └── RetrofitClient.kt               # OkHttp CertificatePinner + Retrofit 싱글턴
    │   │
    │   └── repository/
    │       ├── AuthRepository.kt               # 인증 API 인터페이스 (login, register, logout, refreshToken)
    │       ├── UserRepository.kt               # 유저 API 인터페이스 (getMyProfile, updateMyProfile, uploadAvatar, changePassword, deleteAccount, getPublicProfile)
    │       ├── ProductRepository.kt            # 상품 API 인터페이스 (getProducts, getProductById, getMyProducts, getLikedProducts, getProductsByUser, registerProduct, updateProduct, deleteProduct, updateProductStatus, uploadProductImages, likeProduct, unlikeProduct)
    │       │
    │       └── mock/                           # Mock 구현체 — 백엔드 연결 시 Remote 구현체로 교체
    │           ├── MockAuthRepository.kt       # 인증 Mock (딜레이 + 더미 토큰 반환)
    │           ├── MockUserRepository.kt       # 유저 Mock (MockData 기반 프로필)
    │           └── MockProductRepository.kt    # 상품 Mock (likedIds 내부 상태, myProducts 뮤터블 리스트)
    │
    ├── security/
    │   └── RootDetector.kt                     # 루팅 탐지 Kotlin 래퍼 (네이티브 호출 + 패키지 검사)
    │
    └── ui/
        ├── components/                         # 재사용 공통 컴포넌트
        │   ├── BottomNavBar.kt                 # 하단 탭바 (홈/찜/마이페이지)
        │   └── ProductCard.kt                  # 상품 카드 (이미지, 이름, 가격, 찜 버튼)
        │
        ├── navigation/
        │   └── AppNavigation.kt                # Route sealed class + NavHost 전체 라우트 정의
        │
        ├── screens/
        │   ├── splash/
        │   │   └── SplashScreen.kt             # 앱 시작 스플래시 (자동 전환)
        │   │
        │   ├── onboarding/
        │   │   └── OnboardingScreen.kt         # 온보딩 (로그인/회원가입 진입)
        │   │
        │   ├── auth/
        │   │   ├── AuthViewModel.kt            # 로그인·회원가입 상태 관리
        │   │   ├── LoginScreen.kt              # 로그인 화면
        │   │   └── SignUpScreen.kt             # 회원가입 화면
        │   │
        │   ├── home/
        │   │   ├── HomeViewModel.kt            # 홈 상품 목록 로드 + toggleLike
        │   │   └── HomeScreen.kt               # 홈 (배너 캐러셀, 바로가기, 추천 상품 그리드)
        │   │
        │   ├── wishlist/
        │   │   ├── WishlistViewModel.kt        # 찜 목록 로드 + unlike
        │   │   └── WishlistScreen.kt           # 찜한 상품 목록 화면
        │   │
        │   ├── mypage/
        │   │   ├── UserViewModel.kt            # 프로필 조회·수정·비밀번호변경·탈퇴 상태 관리
        │   │   ├── ProductViewModel.kt         # 내 상품 목록 상태 관리
        │   │   ├── MyPageScreen.kt             # 마이페이지 (프로필 + 내 상품 그리드, 필터/정렬)
        │   │   └── ProfileEditScreen.kt        # 프로필 수정 (아바타, 닉네임, 지역, 비밀번호 변경, 회원탈퇴)
        │   │
        │   ├── product/
        │   │   ├── ProductDetailViewModel.kt   # 상세 조회 + 좋아요 + 삭제 + 상태변경
        │   │   ├── ProductDetailScreen.kt      # 상품 상세 (이미지 캐러셀, 판매자, 하단 찜/채팅/구매, MoreVert 메뉴)
        │   │   ├── ProductEditViewModel.kt     # 기존 상품 로드 후 수정 저장
        │   │   ├── ProductEditScreen.kt        # 상품 수정 폼 (상품명/가격/카테고리/설명)
        │   │   ├── ProductRegisterViewModel.kt # 새 상품 등록 상태 관리
        │   │   ├── ProductRegisterScreen.kt    # 상품 등록 폼
        │   │   └── AutoPriceScreen.kt          # 자동 시세 조회 화면
        │   │
        │   └── userprofile/
        │       ├── UserProfileViewModel.kt     # 타 유저 프로필 + 판매 상품 목록 로드
        │       └── UserProfileScreen.kt        # 타 유저 프로필 화면 (아바타, 닉네임, 지역, 판매 상품 그리드)
        │
        └── theme/
            ├── Color.kt                        # BrandPurple, BrandGray, BrandLightGray 등 앱 색상
            ├── Theme.kt                        # MaterialTheme 설정
            └── Type.kt                         # 타이포그래피 설정
```

---

## 백엔드 연결 시 교체 포인트

각 ViewModel 상단 주석에 명시되어 있음:
```kotlin
// 백엔드 연결 시: MockXxxRepository() → RemoteXxxRepository(retrofit)
```

교체 순서:
1. `RemoteAuthRepository`, `RemoteUserRepository`, `RemoteProductRepository` 구현 (Retrofit)
2. 각 ViewModel 생성자의 `Mock~` → `Remote~` 로 교체
3. Data 레이어 외 수정 불필요
