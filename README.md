# 🛡️ Spring Boot OAuth2 + JWT 통합 인증 템플릿

JWT 기반 자체 로그인 + OAuth2(Google, Naver, Kakao) 로그인 + Refresh Token 기반 인증 재발급까지 통합적으로 지원하는 백엔드 인증 템플릿입니다. 매번 반복되는 인증 로직을 추상화하여 빠르게 프로젝트에 적용할 수 있도록 구성했습니다.

---

## 🛠️ 기술 스택

- Spring Boot 3.x
- Spring Security 6.x
- OAuth2 Client (Google, Naver, Kakao)
- JWT (Access / Refresh Token 분리 전략)
- MySQL
- JPA (Hibernate)
- REST API

---

## 🔐 전체 인증 흐름
이 프로젝트는 자체 로그인과 OAuth2 로그인(Google, Naver, Kakao) 을 모두 지원하며, JWT 기반 인증 구조를 따릅니다. 인증은 크게 2단계 회원가입 절차와 Access / Refresh Token 관리 흐름을 기반으로 설계되어 있습니다.

---
## 🧾 회원가입 및 인증 절차
1. 자체 로그인 방식
- 사용자가 username과 password를 입력하여 로그인 요청을 보냅니다.
- 인증에 성공하면 즉시 Access Token과 Refresh Token을 발급하며, 해당 Refresh Token은 사용자-기기(deviceId) 조합을 기준으로 DB에 저장됩니다.
- 다만, 사용자가 아직 회원가입 추가 정보(nickname, email 등)를 입력하지 않은 상태라면, NOT_REGISTERED 권한으로 간주하고 임시 access token만 발급됩니다. 이 토큰을 통해 제한된 API에 접근할 수 있고, 이후 추가 정보 입력을 통해 USER 권한으로 승격됩니다.
  
2. OAuth2 로그인 방식 (Google / Naver / Kakao)
- 사용자는 원하는 소셜 로그인 버튼을 클릭해 OAuth2 인증 절차를 수행합니다.
- 인증 성공 후, Spring Security의 OAuth2SuccessHandler가 호출됩니다. 이 시점에 사용자가 최초 로그인이라면 DB에 NOT_REGISTERED 권한으로 유저가 등록됩니다.
- 이후 우리 서버는 사용자에게 리다이렉트 URL을 전달해야 하는데, 이 과정에서는 보안상 Authorization 헤더를 직접 조작할 수 없기 때문에, 임시 access token을 쿼리 파라미터로 포함시켜 프론트에 전달합니다.
- 클라이언트는 이 임시 access token을 활용하여 최종 회원가입을 완료하고, 이후에야 정식 access + refresh token을 발급받게 됩니다.
---

## 🔁 Refresh Token 발급 전략
- 이 프로젝트는 RTR(Rotate-Then-Reuse) 전략을 기반으로 refresh token을 관리합니다.
- 클라이언트가 refresh token을 사용해 access token을 갱신할 때마다 기존 refresh token은 폐기되며, 새로운 access + refresh token 쌍이 함께 발급됩니다.
- refresh token은 userId + deviceId 기준으로 DB에 저장되며, 보안을 위해 재사용되지 않습니다.

---
## 🧱 역할 기반 인가
- 사용자의 권한은 Role로 구분됩니다. Spring Security 설정에서 해당 권한에 따라 접근 제어를 수행하며,
AccessDeniedHandler를 커스터마이징하여, 권한 부족 시 사용자에게 명확한 응답 메시지를 제공하도록 구성했습니다.
  - NOT_REGISTERED: 회원가입 추가 정보를 입력하지 않은 사용자
  - USER: 정식 회원가입이 완료된 사용자

---
## 🧪 Swagger 문서화
개발 편의를 위해 Swagger UI를 간단히 추가하였습니다.
주요 인증 API(username 로그인, 회원가입, 토큰 재발급 등)를 테스트할 수 있도록 문서화되어 있어, 팀원이나 프론트엔드 개발자가 쉽게 테스트할 수 있습니다.

