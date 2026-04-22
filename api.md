# API 명세 & 코드 연결 현황

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| 백엔드 | Python + FastAPI |
| 안드로이드 HTTP 클라이언트 | Retrofit |
| 통신 포맷 | JSON |

> **주의**: FastAPI Pydantic 모델 필드명과 Android data class 필드명을 반드시 일치시킬 것
> (불일치 시 Retrofit이 null로 역직렬화함)

---

> **범례**
> - 상태 `✅ 연결됨` / `🔧 Mock 구현` / `⬜ 미구현`
> - **코드 위치**: 백엔드 연결 시 교체할 파일 경로

---
● 인증 섹션 수정본:

  ## 인증 / 계정

  | 메서드 | 엔드포인트 | 설명 | 상태 | 코드 위치 |
  | --- | --- | --- | --- | --- |
  | POST | `/auth/register` | 회원가입 | 🔧 Mock 구현 | `data/repository/AuthRepository.kt` → `register()` |
  | POST | `/auth/login` | 로그인 (JWT 발급) | 🔧 Mock 구현 | `data/repository/AuthRepository.kt` → `login()` |
  | POST | `/auth/logout` | 로그아웃 (토큰 무효화) | 🔧 Mock 구현 | `data/repository/AuthRepository.kt` → `logout()` |
  | POST | `/auth/refresh` | 액세스 토큰 갱신 | 🔧 Mock 구현 | `data/repository/AuthRepository.kt` → `refreshToken()` |

  **관련 UI**: `ui/screens/auth/LoginScreen.kt`, `ui/screens/auth/SignUpScreen.kt`
  **ViewModel**: `ui/screens/auth/AuthViewModel.kt`
  **Mock**: `data/repository/mock/MockAuthRepository.kt`
  **교체 포인트**: `MockAuthRepository` → `RemoteAuthRepository` (Retrofit) — `AuthViewModel` 생성자만 바꾸면 됨

  사용자/프로필 섹션 수정본:

  ## 사용자 / 프로필

  | 메서드 | 엔드포인트 | 설명 | 상태 | 코드 위치 |
  | --- | --- | --- | --- | --- |
  | GET | `/users/me` | 내 프로필 조회 | 🔧 Mock 구현 | `data/repository/UserRepository.kt` → `getMyProfile()` |
  | PATCH | `/users/me` | 내 프로필 수정 (닉네임, 지역 등) | 🔧 Mock 구현 | `data/repository/UserRepository.kt` →
  `updateMyProfile()` |
  | POST | `/users/me/avatar` | 프로필 이미지 업로드 | 🔧 Mock 구현 | `data/repository/UserRepository.kt` →
  `uploadAvatar()` |
  | PUT | `/auth/password/change` | 비밀번호 변경 | 🔧 Mock 구현 | `data/repository/UserRepository.kt` →
  `changePassword()` |
  | DEL | `/users/me` | 회원 탈퇴 | 🔧 Mock 구현 | `data/repository/UserRepository.kt` → `deleteAccount()` |
  | GET | `/users/{user_id}` | 타 유저 공개 프로필 조회 | ⬜ 미구현 | UI 없음 (타 유저 프로필 화면 필요) |
  | GET | `/users/{user_id}/reviews` | 유저 거래 후기 목록 | ⬜ 미구현 | UI 없음 (후기 목록 화면 필요) |
  | GET | `/users/{user_id}/products` | 유저가 올린 상품 목록 | 🔧 Mock 구현 | `data/repository/UserRepository.kt` →
  `getUserProducts()` |

  **관련 UI**: `ui/screens/mypage/MyPageScreen.kt`, `ui/screens/mypage/ProfileEditScreen.kt`
  **ViewModel**: `ui/screens/mypage/UserViewModel.kt`
  **Mock**: `data/repository/mock/MockUserRepository.kt`
  **교체 포인트**: `MockUserRepository` → `RemoteUserRepository` (Retrofit)


##상

  | GET | `/products` | 상품 목록 조회 (필터, 검색, 페이지네이션) | 🔧 Mock 구현 |
  `data/repository/ProductRepository.kt` → `getProducts()` |
  | GET | `/products/{product_id}` | 상품 상세 조회 | ⬜ 미구현 | UI 없음 (상품 상세 화면 필요) |
  | POST | `/products` | 상품 등록 | 🔧 Mock 구현 | `data/repository/ProductRepository.kt` → `registerProduct()` |
  | PATCH | `/products/{product_id}` | 상품 수정 | ⬜ 미구현 | UI 없음 (상품 수정 화면 필요) |
  | DEL | `/products/{product_id}` | 상품 삭제 | ⬜ 미구현 | UI 없음 (상세 화면에 추가 예정) |
  | POST | `/products/{product_id}/images` | 상품 이미지 업로드 | 🔧 Mock 구현 | `data/repository/ProductRepository.kt`
  → `uploadProductImages()` |
  | PATCH | `/products/{product_id}/status` | 상태 변경 (판매중 / 예약중 / 판매완료) | ⬜ 미구현 | UI 없음 (상세 화면에
  추가 예정) |
  | POST | `/products/{product_id}/like` | 찜하기 | 🔧 Mock 구현 | `data/repository/ProductRepository.kt` →
  `likeProduct()` |
  | DEL | `/products/{product_id}/like` | 찜 취소 | 🔧 Mock 구현 | `data/repository/ProductRepository.kt` →
  `unlikeProduct()` |
  | GET | `/products/liked` | 내 찜 목록 | 🔧 Mock 구현 | `data/repository/ProductRepository.kt` → `getLikedProducts()`
  |
  | GET | `/products/me` | 내가 올린 상품 목록 | 🔧 Mock 구현 | `data/repository/ProductRepository.kt` →
  `getMyProducts()` |

  **관련 UI**: `ui/screens/home/HomeScreen.kt`, `ui/screens/wishlist/WishlistScreen.kt`,
  `ui/screens/product/ProductRegisterScreen.kt`, `ui/screens/mypage/MyPageScreen.kt`
  **ViewModel**: `ui/screens/home/HomeViewModel.kt`, `ui/screens/wishlist/WishlistViewModel.kt`,
  `ui/screens/product/ProductRegisterViewModel.kt`, `ui/screens/mypage/ProductViewModel.kt`
  **Mock**: `data/repository/mock/MockProductRepository.kt`
  **교체 포인트**: `MockProductRepository` → `RemoteProductRepository` (Retrofit)

## 검색

| 메서드 | 엔드포인트 | 설명 | 상태 | 코드 위치 |
| --- | --- | --- | --- | --- |
| GET | `/search` | 통합 검색 (상품 + 유저) | ⬜ 미구현 | - |

---

## 카테고리 / 지역

| 메서드 | 엔드포인트 | 설명 | 상태 | 코드 위치 |
| --- | --- | --- | --- | --- |
| GET | `/categories` | 카테고리 목록 (전자기기, 의류 등) | ⬜ 미구현 | - |
| GET | `/regions` | 지역 목록 (동네 기반 필터용) | ⬜ 미구현 | - |

---

## 채팅

> 채팅은 WebSocket으로 구현 예정

| 메서드 | 엔드포인트 | 설명 | 상태 | 코드 위치 |
| --- | --- | --- | --- | --- |
| GET | `/chats` | 내 채팅방 목록 | ⬜ 미구현 | - |
| POST | `/chats` | 채팅방 개설 (상품 기준) | ⬜ 미구현 | - |
| DEL | `/chats/{chat_id}` | 채팅방 나가기 | ⬜ 미구현 | - |
| GET | `/chats/{chat_id}/messages` | 채팅 메시지 목록 조회 | ⬜ 미구현 | - |
| POST | `/chats/{chat_id}/messages` | 메시지 전송 (REST fallback) | ⬜ 미구현 | - |
| POST | `/chats/{chat_id}/images` | 채팅 내 이미지 전송 | ⬜ 미구현 | - |

---

## 거래 / 후기

| 메서드 | 엔드포인트 | 설명 | 상태 | 코드 위치 |
| --- | --- | --- | --- | --- |
| POST | `/trades` | 거래 확정 | ⬜ 미구현 | - |
| GET | `/trades/me` | 내 거래 내역 | ⬜ 미구현 | - |
| POST | `/trades/{trade_id}/review` | 거래 후기 작성 | ⬜ 미구현 | - |

---

## 운영 / 공통

| 메서드 | 엔드포인트 | 설명 | 상태 | 코드 위치 |
| --- | --- | --- | --- | --- |
| GET | `/health` | 서버 헬스체크 (배포 필수) | ⬜ 미구현 | - |
| GET | `/notices` | 공지사항 목록 | ⬜ 미구현 | - |
| GET | `/notices/{notice_id}` | 공지사항 상세 | ⬜ 미구현 | - |

---

## 관리자

| 메서드 | 엔드포인트 | 설명 | 상태 | 코드 위치 |
| --- | --- | --- | --- | --- |
| GET | `/admin/users` | 전체 유저 관리 | ⬜ 미구현 | - |
| PATCH | `/admin/users/{user_id}/ban` | 유저 정지 / 해제 | ⬜ 미구현 | - |
| GET | `/admin/products` | 전체 상품 관리 | ⬜ 미구현 | - |
| POST | `/admin/notices` | 공지사항 작성 | ⬜ 미구현 | - |
| PATCH | `/admin/notices/{notice_id}` | 공지사항 수정 | ⬜ 미구현 | - |
| DEL | `/admin/notices/{notice_id}` | 공지사항 삭제 | ⬜ 미구현 | - |
| POST | `/admin/upload` | 공지 / 배너 파일 업로드 | ⬜ 미구현 | - |

claude --resume 5c65fbce-59ad-46d7-8777-87c1a9f227ff
