# WORKLOG - 윈터푸디스 백엔드 (01_winter_back)

## 2026-10-26 11:55 프로젝트 재시작 - 기존 코드 점검 및 기준점 정리

### 배경
- 2025-10 이후 중단된 윈터푸디스(겨울 길거리 음식 노점 찾기·주문 앱)를 다시 진행
- 방향: 백엔드(Spring Boot)는 정리해서 유지, Flutter 앱은 새 프로젝트로 재작성(기존 화면은 디자인 참고용)

### 백엔드 점검 결과 (요약)
- 구조: 도메인별 controller → service(impl) → dao(MyBatis) → dto, MyBatis XML 매퍼 15개, JPA 없음, MariaDB + Redis
- 고객 API(가게·카테고리·메뉴·장바구니·찜·배송지·주문·리뷰)는 동작 수준, **사장님 API는 없음**
- 문제: 회사 샘플에서 넘어온 세션 방식 시큐리티 잔재, 필터 예외가 500으로 나감, refresh 토큰 흐름이 사실상 동작 안 함, 주문 금액을 클라이언트 값 그대로 저장(위변조 가능), 페이징 offset 버그(storeCustom.xml), 메뉴 조회 N+1
- 비밀값: JWT 시크릿·RDS 계정은 원격(GitHub)에 올라간 적 없음(로컬 미커밋 변경분에만 존재), 원격에는 로컬 도커 DB 비밀번호만 있음
- 버전: Spring Boot 2.6.7 / Java 17. Boot 3.5 OSS 지원이 2026-06-30 종료돼 목표를 **4.1로, 2.6 → 3.5 → 4.1 두 단계**로 정함

### 0단계 작업
- `legacy-v1` 태그(c7d9571, 원격과 동일 상태) + `refactor/backend-cleanup` 브랜치 생성
- 작업 중이던 변경분 기준점 커밋: 매장 이미지를 `tb_store_image` LEFT JOIN 으로 조회하도록 변경, 장바구니·찜·주문·리뷰 쿼리에서 `tb_store.image_url` 참조 제거, 매퍼 쿼리에 SQL ID/SQL 명 주석 추가
  - 커밋에서 제외: `application.yml`(JWT 시크릿 포함 → 2단계 환경변수화 후 커밋), `application-dev.yml`(RDS 계정, 커밋 금지), `SecurityConfig_old`·주석뿐인 `auth/dao/UserDao`(1단계 삭제 대상)
- 로컬 개발 DB 구성: `docker-compose.yml`(MariaDB 11.4 포트 3307 / DB `winter_app`, Redis 7.4), 비밀번호는 로컬 `.env`(git 제외)로 주입
- **DB 스키마 복원**: RDS 접속 불가(보안그룹 차단 추정, EC2도 응답 없음, SSH 키 없음) → 매퍼 XML·DTO 기반으로 `db/init/01_schema.sql` 작성(테이블 26개, 추정 컬럼 53곳 `-- 추정` 표기)
  - 검증: 컨테이너 최초 기동 시 자동 적용, 매퍼 SQL 126개(정렬 분기 포함 131건)를 실제 DB에서 실행 → **131/131 통과**(쓰기는 트랜잭션 후 ROLLBACK)
  - 발견: 이미지 2장 이상이면 매장 단건 조회 TooManyResults·목록 중복, 매장 등록 쿼리가 DTO에 없는 필드(business_number, is_featured) 사용, 가입 시 `ROLE_ADMIN` 권한 행 부재로 권한 조회 빈 결과, 찜 재등록 시 중복 행, 리뷰 매퍼 user_id 숫자/문자 혼용

### 커밋
- `[Refactor] 매장 이미지 tb_store_image 분리 조회 및 SQL ID 주석 추가` (8파일)

## 2026-10-26 12:16 1단계 - 죽은 코드 삭제 + 로컬 DB 구성 커밋

### 로컬 개발 DB 구성 (7e1dcd4)
- `docker-compose.yml`: MariaDB 11.4(호스트 3307 → DB `winter_app`, utf8mb4) + Redis 7.4, root 비밀번호는 로컬 `.env`(git 제외)에서 주입, `db/init` 을 initdb 디렉터리로 마운트
- `db/init/01_schema.sql`: 매퍼·DTO 역설계 DDL, 최종 22개 테이블(사용자·인증 / 매장 / 음식 메뉴 / 찜·장바구니 / 주문·리뷰). FK 제약은 걸지 않고 주석으로 관계 표기, 추정 컬럼은 `-- 추정`
  - 1단계에서 쿼리가 사라진 `tb_api`·`tb_auth_api_map`·`tb_menu`(관리자 화면 메뉴)·`tb_auth_menu_map` 4개 제외
- `db/init/02_seed.sql`: `tb_auth` 기본 역할 3개(ROLE_ADMIN / ROLE_USER / ROLE_OWNER)
  - 이유: 역할 조회가 `tb_auth` INNER JOIN 이라 행이 없으면 권한이 빈 문자열로 토큰에 실리고, `TokenProvider.getAuthentication` 에서 `SimpleGrantedAuthority("")` 예외 → 모든 인증 API가 401

### 죽은 코드 삭제 (e84512e, 30파일 +8 / -1665)
- 삭제 23개
  - 세션 로그인 시절 보안: AuthLoginProvider(@Component AuthenticationProvider), Login 성공/실패/로그아웃 핸들러, AuthDynamicCreateUserHandler, AuthorizationChecker, SessionAttributeManager, ComConstantAuth
  - 관리자 화면 메뉴·API 권한 체계: SecuritySupportSvc(+Impl), RoleApiAuthMapg, RoleMenuScrnMapg, ComLoginUserMenuDto, ComApiAuthPatternDto, ComConstant
  - 기타: RestTemplate 설정 3종, JsonUtils, Test 컨트롤러, KeyGenerator, (미커밋) SecurityConfig_old, 주석뿐인 auth/dao/UserDao
- 연쇄 정리: AuthService 로그인·토큰 갱신에서 메뉴 권한 조회(`getRoleMenuScrnRoleUse` + 빈 리스트만 돌려주던 `convertToMenuDto`) 제거, SecuritySupportDao·commonSecurity.xml 메뉴/API 쿼리 3개 제거, SecurityBeanConfig 빈 6개 정리, ComLoginUserInfoDto 메뉴·MQTT·API 권한 필드 제거, AccessDeniedHandler 로그를 요청 URI 로 변경
- `SecurityConfig.authenticationManagerBean()` 제거: AuthenticationProvider 삭제 후 기동 시 "dependency cycle ... AuthenticationManager" 오류 발생. 로그인은 AuthService 가 BCrypt 로 직접 검증하므로 AuthenticationManager 불필요
- pom: jsch(SSH 터널, 설정도 주석), spring-session-data-redis(STATELESS 인데 잔존), gson 제거

### 검증
- `./mvnw compile` 성공, 로컬 MariaDB/Redis 에 붙여 기동 4초
- API 스모크: 회원가입 / 로그인(오답 비밀번호 400) / 내 정보 / 장바구니 / 토큰 갱신(access 유효 상태) / 로그아웃 후 동일 토큰 401 → 모두 정상

### 남은 이슈 (다음 라운드)
- 회원가입 시 모든 사용자에게 ROLE_ADMIN 부여(user.xml regUserAuthMap) → 손님/사장님 역할 분리 시 수정
- 권한이 비면 토큰 인증이 예외로 깨지는 TokenProvider 방어 로직 필요

## 2026-10-26 12:22 미사용 클래스 추가 삭제 (14f80c4, 4파일 -321)
- 삭제: `ComLoginUserSimpleInfoDto`(메뉴 권한 정리 후 참조 0), 빈 인터페이스 `MenuCategoryDao`(매퍼 XML 없음), `MenuCategoryDto`, `DateUtil`(정적 메서드 21개, 호출처 0)
- 방법: 전체 클래스명을 java·resources 에서 단어 단위 grep → 참조 0 인 것 중 Spring 이 직접 등록하는 Controller·Service·Config·Application 은 제외하고 선별
- `OrderStatus`(상태 문자열 상수)는 미사용이지만 다음 라운드 주문 상태 enum 정리에 쓸 예정이라 유지
- 검증: `./mvnw clean compile` 성공
- 기타: 윈터푸디스는 프로젝트 구글시트를 만들지 않기로 함(요청 시 생성) → 커밋 기록은 WORKLOG + 소스 번들만

## 2026-10-26 12:30 2단계 - DB·JWT 비밀값 환경변수화 (cd873e9)

### 사전 확인 (git 이력 전체 검색, 값은 출력하지 않음)
- 원격 이력에 있는 것: 로컬 도커 DB 비밀번호, 미사용 로그인 secret-key 뿐
- JWT 시크릿·RDS 주소/계정/비밀번호는 이력에 없음(미커밋 변경분·미추적 application-dev.yml 에만 존재)

### 변경
- `application.yml`
  - `spring.datasource.url/username/password` → `${DB_URL:로컬 도커 기본값}` / `${DB_USERNAME:root}` / `${DB_PASSWORD}`(필수)
  - `jwt.secret` → `${JWT_SECRET}`(필수), `spring.redis.host/port` → `${REDIS_HOST:localhost}` / `${REDIS_PORT:6379}`
  - `spring.config.import: optional:file:.env[.properties]` → 로컬은 프로젝트 루트 `.env` 를 프로퍼티로 읽음(docker-compose 도 같은 .env 사용), 배포는 실제 환경변수 주입
  - 삭제: 주석 SSH 터널·RDS 접속 정보, 미사용 `spring.jpa`, 코드에서 안 읽는 `spring.security` 키(application-name, fully-authenticated-url-patterns, login.*, logout.*, reload-role-fixed-rate-string, metrics)
  - 이전 작업분 로그 레벨(org.springframework.jdbc.core / org.mybatis DEBUG) 포함
- `.env.example` 추가(키 이름·생성 명령만), 로컬 `.env` 에 DB_ROOT_PASSWORD / DB_PASSWORD / JWT_SECRET
- JWT 시크릿 재발급: HS512 용 64바이트 랜덤(Base64). openssl 출력이 64자에서 줄바꿈돼 48바이트로 잘리는 문제가 있어 python secrets 로 재생성 → 기존 발급 토큰은 무효
- `application-dev.yml`(RDS 계정)은 `.git/info/exclude` 로 커밋 차단

### 검증
- 셸 환경변수 없이 `.env` 만으로 기동 성공(4.7초)
- 로그인 / 내 정보 / 가게 목록 / 토큰 갱신 / 로그아웃 정상
- 커밋 대상·HEAD 의 application.yml 에 평문 password/secret 없음 확인


## 2026-10-26 12:48 dev 설정 정리 + 설정 DTO 삭제 (473a649)
- `application-dev.yml`(미추적, RDS 계정) → 로컬 전용 `.env.dev`(DB_URL / DB_USERNAME / DB_PASSWORD, dev용 JWT_SECRET 은 빈칸)로 옮기고 삭제. jar 빌드 시 RDS 비밀번호가 포함되는 문제 해소
- 설정 키 정리로 미사용이 된 `ComSecurityLoginInfoDto` / `ComSecurityLogoutInfoDto` / `ComSecurityUserInfoDto` 삭제, `ComSecurityInfoDto` 는 staticContentsUrlPatterns / permitAllUrlPatterns 만 유지

## 2026-10-26 12:48 3a단계 - Spring Boot 2.6.7 → 3.5.16 업그레이드 (0c90879, 32파일 +131 / -153)

### 버전 결정 (Maven Central 메타데이터로 확인)
- Spring Boot 3.5.16(3.x 마지막 OSS 릴리스), Java 17 → 21
- mybatis-spring-boot-starter 3.0.5(Boot 3.5 기준 빌드), jjwt 0.12.7, p6spy-spring-boot-starter 1.12.1(Boot 3용, 2.x 는 Boot 4용)
- mariadb-java-client·commons-lang3·lombok 고정 버전 제거 → Boot 관리 버전 사용, 버전은 pom properties 로 모음

### 코드 변경
- javax.servlet / validation / annotation → jakarta (26개 파일, javax.crypto 등은 유지)
- `SecurityConfig`: WebSecurityConfigurerAdapter 상속 → `SecurityFilterChain` + `WebSecurityCustomizer` 빈, 람다 DSL(`csrf`/`cors`/`exceptionHandling`/`sessionManagement`/`authorizeHttpRequests`), `antMatchers` → `requestMatchers`. CORS 는 CorsFilter 수동 등록 대신 `cors(withDefaults())` 가 CorsConfigurationSource 빈 사용. 정적 경로 패턴이 비어 있으면 ignoring 생략. 허용 경로·동작은 기존과 동일하게 유지
- `TokenProvider`(jjwt 0.12): Key → SecretKey, `setSubject/setIssuedAt/setExpiration` → `subject/issuedAt/expiration`, `signWith(key, SignatureAlgorithm.HS512)` → `signWith(key, Jwts.SIG.HS512)`, `parserBuilder().setSigningKey().parseClaimsJws().getBody()` → `parser().verifyWith().parseSignedClaims().getPayload()`
- SQL 로그: log4jdbc(DriverSpy, jdbc:log4jdbc URL, log4jdbc.log4j2.properties) → p6spy 스타터 + 순수 MariaDB 드라이버. logback 의 jdbc.* 로거 → `p6spy` 로거
- `spring.redis.*` → `spring.data.redis.*` (Boot 3 키 변경)
- 컴파일 deprecated 경고 0건

### 검증
- 기동 5초, 로컬 DB 에 가게·메뉴·옵션 테스트 데이터 적재 후 API 32개 스모크(가입~가게~메뉴~찜~배송지~장바구니~주문~리뷰~토큰 갱신~로그아웃)
- **업그레이드 전 커밋(cd873e9)을 임시 worktree 로 8086 포트에 동시 기동해 같은 스크립트로 비교** → 양쪽 모두 28/32, 실패 4건 동일(기존 버그). 주요 조회 API 8개는 응답 JSON 완전 일치 → 회귀 없음
- 기존 버그(다음 라운드): 가게 랭킹 `type` 미지정 시 NPE, 배송지 등록 요청 DTO 의 userId @NotBlank 로 등록 불가, 리뷰 작성 응답이 JSON 이 아닌 문자열(String 반환 + ResponseWrapper)
- 발견·수정: p6spy 기본 로그 포맷이 접속 URL(계정·비밀번호 포함)을 출력 → URL 제외한 `log-format` 지정, 재기동 후 로그 내 비밀번호 0건 확인
- 참고: 수정 전 기동 로그가 로컬 `C:\app\winterfoodies\back\log\application.log` 에 남아 있음(로컬 도커 DB 비밀번호)

## 2026-10-26 12:56 3b단계 - Spring Boot 3.5.16 → 4.1.2 업그레이드 (00414dd, 8파일 +27 / -19)

### 버전 결정
- 공식 마이그레이션 가이드(Spring Boot 4.0 Migration Guide)와 Maven Central 메타데이터로 확인
- Spring Boot 4.1.2(OSS 지원 약 2027년 중반까지) → Spring Framework 7 / Spring Security 7.1 / Spring Data 2026.0 / Jackson 3.1
- mybatis-spring-boot-starter 4.1.0(Boot 4.1 기준 빌드), p6spy-spring-boot-starter 2.0.1(Boot 4용), jjwt 0.12.7 유지

### 변경
- 스타터 모듈화: `spring-boot-starter-web` → `spring-boot-starter-webmvc`, 테스트는 `spring-boot-starter-webmvc-test` / `spring-boot-starter-security-test`
- Jackson 3: 보안 핸들러(AccessDenied / EntryPoint / SecurityBeanConfig)의 `ObjectMapper` 를 `tools.jackson.databind` 로. 어노테이션(`com.fasterxml.jackson.annotation`)은 패키지 유지라 DTO 무수정
- **ResponseWrapper 응답 래핑 기준 변경**: `supports()` 가 `MappingJackson2HttpMessageConverter` 를 검사하고 있었는데 Spring 7 은 `JacksonJsonHttpMessageConverter` 를 사용 → 그대로면 컴파일은 되지만 모든 응답이 `{code, desc, status, data}` 로 감싸지지 않는 무증상 장애가 됨. `JacksonJsonHttpMessageConverter` 로 교체
- Redis 캐시: `GenericJackson2JsonRedisSerializer`(Boot 4 에서 제거) → `GenericJacksonJsonRedisSerializer.builder().enableDefaultTyping(...)`. 캐시 값(TokenVo) 타입 복원을 위해 타입 정보 저장하되, `BasicPolymorphicTypeValidator` 로 역직렬화 허용 타입을 `com.winter.winter_back.` / `java.util.` / `java.time.` 으로 제한(기존은 모든 타입 허용)
- deprecated 정리: `opsForValue().set(k, v, long, TimeUnit)` → `set(k, v, Duration)`, `@Valid List<OrderItem>` → `List<@Valid OrderItem>`(Hibernate Validator HV000271)

### 검증
- main/test 컴파일 성공, deprecated 경고 0건, 기동 8.7초
- 3.5 커밋(0c90879)을 임시 worktree 로 8086 포트에 동시 기동해 비교
  - API 32개 스모크: 양쪽 28/32 동일(실패 4건은 기존 버그)
  - 조회 API 14개 응답 JSON 완전 일치(날짜 포맷 포함), 인증 실패 401 응답 동일
- Redis 캐시 사용하는 토큰 갱신 정상, 주문 항목 중첩 검증 정상(메뉴 ID 누락 시 400), p6spy SQL 로그 정상·비밀번호 노출 0건

### 백엔드 정리 라운드 결과 (refactor/backend-cleanup, 커밋 8건)
- 0단계 기준점 → 1단계 죽은 코드 삭제(약 2,100줄) → 2단계 비밀값 환경변수화 → 3a Boot 3.5 → 3b Boot 4.1
- 다음 라운드 후보: 가게 랭킹 type 미지정 NPE, 배송지 등록 userId 검증, 리뷰 작성 응답 문자열, 가입 시 ROLE_ADMIN 일괄 부여, 권한 비면 토큰 인증 예외, 페이징 offset 버그, 주문 금액 서버 재계산, 매장 이미지 LEFT JOIN 중복, 예외 체계 통일·실제 HTTP 상태 코드, CORS·actuator·/reviews 쓰기 권한

## 2026-10-26 13:03 버그 수정 라운드 0단계 - 스모크 회귀 테스트 추가 (230d844)

### 수정 라운드 계획 (확정)
- 0단계 회귀 테스트 → 1묶음 인증·권한 → 2묶음 기능 오류 → 3묶음 주문 무결성 → 4묶음 보안 설정 → 5묶음 예외·응답 체계 → 6묶음 성능(선택)
- 결정: ① 에러 응답을 실제 HTTP 상태 코드(400/401/404/500)로 ② 매장 이미지는 목록·상세 모두 대표 1장(sort_order 최소) ③ 리뷰-주문 연결 키를 문자열 주문번호(tb_order.order_id)로 통일

### 추가 파일
- `scripts/smoke_test.py`: 표준 라이브러리(urllib)만 사용. 35건 순차 호출(가입 → 로그인 / 비밀번호 오류 → 내 정보 → 카테고리·가게 목록/검색/상세/메뉴/랭킹·메뉴 상세·홈 피드 → 찜 → 배송지 → 장바구니 → 주문 생성/목록/상세/추적 → 리뷰 → 주문 취소·찜 해제·장바구니 비우기 → 토큰 갱신 → 로그아웃 → 로그아웃 토큰 거부 → 토큰 없는 호출 거부)
  - 응답 code 를 기대값과 비교(기본 20000000, 실패가 정상인 케이스는 60001004 / 40001001 지정), 하나라도 실패하면 종료 코드 1, `--base-url` / `--user-id` 옵션
- `db/test/01_test_data.sql`: 매장 2·이미지·통계·운영시간·매장 카테고리·메뉴 카테고리·메뉴 2·옵션 그룹·옵션, 고정 id + INSERT IGNORE 로 재실행 가능

### 기준선 결과
- 32 / 35 통과, 실패 3건은 2묶음 대상(가게 랭킹 type 미지정 NPE / 배송지 등록 userId 검증 / 리뷰 작성 응답 문자열)
- 브라우저 확인 중 추가 발견: 가게 목록 응답에 imageUrl 누락 — `s.*` 의 미사용 tb_store.image_url(NULL)과 `si.image_url` 컬럼명 중복으로 추정 → 2묶음 이미지 수정 때 함께 처리

### 로컬 실행 편의 (커밋 제외)
- 브라우저 패널 서버 실행용 `C:\02.personal\.claude\launch.json` + `.claude/start_server.cmd`(프로젝트 폴더로 이동 후 mvnw 실행. 실행 환경에서 현재 폴더 실행파일 검색이 막혀 있어 mvnw.cmd 를 전체 경로로 호출)

## 2026-10-26 13:13 버그 수정 1묶음 - 인증·권한 (0d12340, 49bc014)

### 회원가입 기본 권한 / 빈 권한 인증 오류 (0d12340)
- `user.xml` regUserAuthMap: 가입 시 `'ROLE_ADMIN'` 고정 → `'ROLE_USER'`(손님). 사장님 가입은 사장님 API 작업 때 별도 처리
- `TokenProvider.getAuthentication`: 권한 claim 이 빈 문자열이면 `SimpleGrantedAuthority("")` 에서 예외 → claim null/빈 값 방어, trim 후 빈 값 제외하고 권한 없이 인증

### 토큰 갱신 흐름 / 토큰 오류 응답 (49bc014)
- 원인: 만료된 access 토큰을 헤더에 실어 `/auth/refresh` 를 호출하면 TokenFilter 가 먼저 만료로 막고, `/auth/refresh` 자체도 인증 필요 경로였음
- `TokenFilter.shouldNotFilter`: `/auth/refresh` 는 필터 검증 생략(검증은 AuthService 가 refresh 토큰 서명·캐시 일치로 수행), SecurityConfig permitAll 에 `/auth/refresh` 추가
- `CacheConfig`: 토큰 캐시(`winter_cache` → 상수 `TOKEN_CACHE_NAME`) TTL 을 `jwt.expire-time.refresh`(1440분)로 지정. 기존 기본 TTL 300분이라 로그인 5시간 후 refresh 실패
- TokenFilter 오류 응답 직접 작성: 필터에서 던진 BizException 은 @RestControllerAdvice 가 못 받아 모두 "로그인 후 시도해주세요(40001001)" 로 뭉개짐 → ExceptionWrapperUtil + ObjectMapper 로 JSON·HTTP 상태를 필터에서 직접 응답. 만료(40001004) / 위조(40001007) / 로그아웃(40001008) 구분 가능 → 클라이언트가 만료일 때만 refresh
- `exception.yml` 누락 키 정리: token_refresh_expire(40001009) / token_refresh_wrong(40001010) / token_empty_auth(40001011) / param_required(30002005) 추가, 코드의 token_expire → token_expired, logout_failed → logout_fail 로 기존 키에 맞춤 (기존엔 refresh 실패가 500 "알 수 없는 오류")

### 검증
- access 만료 1분으로 기동 후 70초 대기해 실제 만료 시나리오 확인: 만료 토큰 40001004 → 만료 토큰으로 refresh 성공 → 새 토큰 정상 / 잘못된 refresh 40001009 / 재사용 refresh 40001010 / 로그아웃 토큰 40001008 / 토큰 없음 40001001
- 신규 가입 권한 ROLE_USER 확인, 권한 행 삭제한 사용자도 로그인 후 API 정상, 토큰 캐시 Redis TTL 86,399초
- 스모크 34 / 37 (위조 토큰·잘못된 refresh 케이스 추가, 실패 3건은 2묶음 대상)
- 남은 점: 서비스 단 에러는 아직 HTTP 200(5묶음), 토큰 없이 /user/me 500(/user/** 전체 공개 → 4묶음)

## 2026-10-26 13:25 버그 수정 2묶음 - 기능 오류 (e3309f8, 08dcbf7, b7d154b, 7d8ddbe)

### 가게 랭킹·배송지 (e3309f8)
- 랭킹: `type.toLowerCase()` NPE → 미지정 시 `popular`, 미지원 종류는 `code_not_found`(30003003, 400). 응답 type 도 정규화 값
- 배송지 등록: 요청 DTO `userId` 의 `@NotBlank` 때문에 항상 400 → 검증 제거(서비스가 토큰 사용자로 set)

### 가게 조회 페이징·대표 이미지 (08dcbf7)
- `storeCustom.xml` 목록 페이징 `OFFSET #{page}` → `#{offset}`(StoreSearchRequest.getOffset = page*size)
- `store.xml` 에 공통 `<sql id="storeColumns">` 추가, storeCustom 은 네임스페이스 전체 경로로 include. 매장 컬럼을 명시하고 대표 이미지는 `(SELECT image_url FROM tb_store_image ORDER BY sort_order, id LIMIT 1) AS image_url` 서브쿼리
  - 기존 문제 1: tb_store_image LEFT JOIN → 이미지 n장이면 매장 n행(단건 TooManyResults, 목록 중복)
  - 기존 문제 2: `s.*` 에 미사용 `tb_store.image_url`(NULL)이 포함돼 `si.image_url` 과 라벨 중복 → JDBC 가 앞 컬럼을 반환해 imageUrl 이 항상 빔(목록·랭킹·홈 피드)
  - 매장 조회 11곳(store 3 + storeCustom 8) 적용
- 테스트 데이터에 1번 매장 이미지 2장(sort_order 1, 2)

### 찜 재등록 중복 (b7d154b)
- 찜 해제는 use_yn='N' 소프트 삭제인데, 재등록 시 새 행 INSERT → 같은 매장 행 누적. 추가 전 `restoreFavorite` 로 해제 행 복구(ORDER BY id DESC LIMIT 1), 없을 때만 INSERT

### 리뷰 응답·리뷰-주문 키 (7d8ddbe)
- 작성·수정·삭제가 String 반환 → ResponseWrapper 가 Jackson 컨버터만 감싸서 문자열 그대로 응답 → void 반환으로 공통 응답 형식
- 결정 ③ 반영: `orderOrderId` Long → String(tb_order.order_id 주문번호). ReviewDto/Request/Response, Dao, Service, Controller(@PathVariable), 스키마 `order_order_id VARCHAR(50)`, 로컬 DB ALTER
- 리뷰 작성 검증 추가: 주문 존재(order_not_found) → 본인·해당 매장 주문(review_not_allowed) → 중복(review_already_exists). 같은 조회 두 번 하던 디버그 코드 제거
- review.xml: `CAST(r.user_id AS CHAR) COLLATE ...` 조인 3곳 → `r.user_id = u.id`(인덱스 사용 가능), deleteAllReviewsByUserId parameterType long → string

### 검증
- 스모크 45/45 통과(종료 코드 0). 추가 케이스: 목록 매장 중복 없음·imageUrl 존재 / 1·2페이지 다른 매장 / 상세 대표 이미지 / 랭킹 미지정·미지원 / 찜 해제→재등록 후 1건 / 실제 주문번호로 리뷰 작성 가능 여부·작성·중복 거부
- 기본 브라우저로 가게 목록 응답 확인(imageUrl 채워짐, 중복 없음)

## 2026-10-26 13:33 버그 수정 3묶음 - 주문 데이터 무결성 (a8950b9, 5파일 +193 / -98)

### 서버 금액 재계산
- 기존: 클라이언트가 보낸 totalAmount·item.price 를 그대로 저장 → 금액 위변조 가능
- `OrderServiceImpl.createOrder`: 매장 조회(StoreDao.findById, 대표 이미지 포함 공통 컬럼) → 항목별 `getUnitPrice` = 메뉴 가격(MenuDao.findById, is_active 만) + 선택 옵션 price_change 합(MenuOptionDao.findByMenuId 로 해당 메뉴의 판매 중 옵션만 Map 화해 소속 검증) → 항목 합계 + 매장 delivery_fee = totalAmount
- 검증: restaurant_not_found / menu_not_found(다른 매장 메뉴 포함) / menu_sold_out / menu_option_not_found / minimum_order_amount(매장 min_order_amount, 메뉴 합계 기준). 에러 키는 기존 yml 재사용
- 요청 totalAmount·deliveryFee·item.price 는 필수 해제(화면 표시 금액과 불일치 시 WARN 로그만), 응답 OrderResponse 에 서버 계산 totalAmount 추가
- 누락 저장 수정: payment_method, created_by 가 insert 파라미터에 없었음
- `catch (Exception)` 제거: 모든 오류를 한글 메시지 BizException 하나로 덮던 문제(원인 코드 소실)

### 주문 상태 enum
- `OrderStatus` 상수 클래스 → enum(description, estimatedTime, `from(String)`, `canCancel()`), DB·응답은 name() 문자열 유지
- 서비스의 상태 문자열 하드코딩 10곳 제거, 상태별 예상 시간 switch → enum 필드

### 검증
- 스모크 49/49: 금액 조작(100원 요청 → 7,000원 저장), 옵션 추가금(9,000원), 다른 매장 메뉴 62001001, 없는 옵션 62002001, 최소 금액 미달 63001007, 주문 상세 저장 금액·결제수단
- DB 직접 확인: total_amount 7000/9000, delivery_fee 1000, payment_method CARD, created_by, 항목 단가 3000/4000(옵션 포함)
- 서버 로그 "주문 금액 불일치 - 요청: 100, 서버 계산: 7000" 확인
- 한계: 주문 항목의 선택 옵션 자체는 저장 테이블이 없어 미저장(단가에만 반영) → 사장님 주문 관리 작업 때 tb_order_item_options 추가 검토

## 2026-10-26 13:41 버그 수정 4묶음 - 보안 설정 (4e947cc, c368f45)

### 비로그인 허용 경로 축소·actuator·CORS (4e947cc)
- 기존: `/user/**`·`/reviews/**`·`/actuator/**` 등을 메서드 무관 전체 공개 → 토큰 없이 리뷰 작성·`/user/me` 호출 시 컨트롤러에서 loginUser null NPE(500)
- `ComSecurityInfoDto` 에 permitAllGetUrlPatterns / permitAllPostUrlPatterns 추가, `SecurityConfig` 가 `requestMatchers(HttpMethod.GET|POST, ...)` 로 적용(설정 비어 있으면 빈 배열)
  - 전체 메서드: `/error`, `/actuator/health`
  - GET 만: `/categories`, `/stores/**`, `/menu/**`, `/home/feed`(비로그인 홈 허용으로 변경), `/reviews/store/**`, `/reviews/{reviewId:\d+}`(숫자만 → `/reviews/my` 는 로그인 필요)
  - POST 만: `/login`, `/auth/refresh`, `/user/register`
  - 코드에 하드코딩된 존재하지 않는 경로(`/auth/login`, `/register`, `/health`) 제거
- actuator: `management.endpoints.web.exposure.include=health`, `show-details=never`
- CORS: `cors.allowed-origin-patterns=${CORS_ALLOWED_ORIGINS:http://localhost:*,http://127.0.0.1:*}`, JWT 헤더 인증이라 `allowCredentials=false`. 모바일 앱은 Origin 미전송이라 영향 없음

### 민감정보 로그 제거 (c368f45)
- 토큰 원문(TokenFilter·getAuthentication·getAccessTokenFromHeader), refresh 요청 본문·로그아웃 토큰(AuthController), claims·Authentication·캐시 TokenVo·새 토큰·사용자 정보(AuthService, 연락처 포함), 암호화 비밀번호(UserServiceImpl) 로그 15줄 삭제
- p6spy SQL 로그가 바인딩 값(비밀번호 해시·연락처)을 출력 → `enable-logging: ${SQL_LOG_ENABLED:false}` 기본 비활성, 로컬 .env 에서만 true (설정은 application.yml 이라 4e947cc 에 포함)

### 검증
- 스모크 52/52: 토큰 없이 /user/me·/reviews/my·POST /reviews → 40001001(이전 500), 비로그인 홈 피드 성공
- /actuator/health 200(UP), /actuator/env 401
- CORS 사전요청: http://localhost:3000 허용, 외부 도메인 403
- 재기동 후 서버 로그 토큰(eyJ) 0건, SQL_LOG_ENABLED=false 인스턴스 p6spy 로그 0줄

## 2026-10-26 13:45 버그 수정 5묶음 - 예외·응답 체계 (2745276, 4ded343)

### 실제 HTTP 상태 코드 (2745276)
- 기존: `ExceptionControllerAdvice` 가 ResponseBaseDto 를 그대로 반환 → 에러도 HTTP 200, 상태는 본문 status 에만
- 모든 핸들러를 `ResponseEntity.status(body.getStatus()).body(body)` 로 변경 (결정 ① 반영). 본문 형식 `{code, desc, status}` 유지 → ResponseWrapper 는 ResponseBaseDto 를 다시 감싸지 않음
- Spring 6.1+ 의 `NoResourceFoundException`(매핑 없는 URL)을 NoHandlerFoundException 키(30003001, 404)로 처리
- 미처리 예외: 응답 desc 에 예외 클래스명 붙이던 것 제거, 대신 `log.error(..., ex)` 로 스택트레이스 기록
- 스모크: 모든 호출에서 본문 status == HTTP 상태 검사 추가, 없는 URL 404 케이스

### 에러 키 통일·예외 클래스 정리 (4ded343, 8파일 +119 / -283)
- 한글 메시지를 에러 키 자리에 넣던 BizException 32곳 → exception.yml 키 (배송지 9: address_not_found / address_limit_exceeded{0} / fail_* , 주문 13: order_not_found / permission_denied / order_cannot_cancel, 리뷰 8: review_not_found / permission_denied / fail_*, 가게 2: restaurant_not_found). 한글 키는 yml 에 없어 전부 "알 수 없는 오류(500)" 였음
- OrderServiceImpl 의 메서드 전체를 감싼 try/catch 5곳 제거(BizException 외 모든 오류를 "~중 오류" 하나로 덮어 원인 소실)
- 장바구니 전용 `BusinessException`·`ErrorCode`(enum 40여 개, 사용처 6곳) → BizException 키로 변환 후 두 파일 삭제. 공통 핸들러 대상이 아니라 500 이던 것이 404/403 으로
- exception.yml: 배송지 섹션(65002xxx) 추가, 배송 코드와 겹치던 즐겨찾기 코드 4개(65001002~5)를 66001002~5 로 이동 → 중복 코드 0

### 검증
- 스모크 53/53 (본문 status·HTTP 상태 일치 포함)
- 없는 주문 63001001 / 없는 장바구니 항목 63002002(이전 500) / 없는 리뷰 64001001 → 모두 HTTP 404

### 수정 라운드 결과
- 0단계~5묶음 완료, 브랜치 커밋 누적 20건. 남은 것: 6묶음 성능(메뉴 N+1, 거리 계산 바운딩박스) - 선택

## 2026-10-26 14:55 현재 위치 메모 (다른 PC 이어하기)
- 백엔드는 버그 수정 0~5묶음까지 완료(4ded343), 6묶음 성능은 보류. 이후 작업은 Flutter 앱 `03_winter_app/docs/WORKLOG.md` 의 "📌 이어서 작업하기" 참고 (환경 준비·실행 순서 포함)

## 2026-10-26 15:50 테스트 데이터 - 강남역 주변 매장 6곳 추가 (1b17706, push 완료)
- 앱 홈 화면(카테고리 그리드·내 주변·인기·신규 섹션)을 카테고리별로 확인하려고 `db/test/01_test_data.sql` 에 매장 3~8 추가: 역삼 어묵포차(EOMUK) / 논현 군밤할매(GUNBAM) / 신논현 고구마굽는집(GUNGOGUMA) / 강남역 계란빵(GYERANPPANG) / 테헤란 타코야키(TAKOYAKI) / 양재 호두과자(HODUGWAJA, CLOSED - 영업 종료 표시 확인용)
- 매장마다 사진(tb_store_image 4~9)·통계·메뉴 카테고리(3~8)·메뉴(3~10) 함께 추가
- 사진 없는 매장 때문에 스모크 테스트 "imageUrl 누락" 실패 → 사진 추가로 해결, 스모크 53/53 통과
- 로컬 DB 는 매장 1·2 이름도 한글(붕어빵 하우스·호떡 코너)로 UPDATE 해 둠 (스크립트 반영은 이미 되어 있음)
- 앱 쪽에서 발견한 후속 이슈: 리뷰 작성 시 tb_store_stats 평점이 갱신되지 않아 목록(통계)과 상세(리뷰 평균) 평점이 다름 / 리뷰 목록 page 는 1부터 / 주문 이력 상태는 이벤트 이름(ORDER_PLACED 등)으로 저장됨

## 2026-10-26 15:53 포장 전용 주문 전환 (주문 방식 orderType 추가) (6b1264f, 11파일 +153 / -14, push 완료)

### 배경
- 붕어빵·호떡 같은 길거리 간식은 배달보다 "미리 주문하고 가서 받기"가 맞음 → 서비스를 **포장(픽업) 전용 + 현장결제**로 정함 (배달은 확장용으로 남김)
- 기존 주문 API 는 배송 주소 필수 + 매장 배달비 항상 부과라 포장 주문을 받을 수 없었음. DB 의 주소·연락처 컬럼은 원래 NULL 허용이라 막고 있던 건 코드뿐

### 구현
- `OrderType` enum 신규: PICKUP(포장) / DELIVERY(배달). 요청에 값이 없으면 PICKUP, 모르는 값이면 null → 에러
- `OrderRequest`: `orderType` 추가, `deliveryAddress` 필수 해제(배달일 때만 서비스에서 검사), 포장 연락처 `contactPhone` 추가
- `OrderServiceImpl.createOrder`
  - 주문 방식 검증: 알 수 없는 값 → `invalid_order_type`(63001009), 배달인데 주소 없음 → `delivery_address_required`(63001010)
  - 배달비: 포장이면 0, 배달이면 매장 배달비 (서버 계산 금액 저장 원칙 유지)
  - 포장 주문은 연락처만 저장 (`delivery_phone` 컬럼을 포장·배달 공통 연락처로 사용, 주석 변경)
- 응답: 주문 목록·상세·내부 DTO 에 `orderType` 추가 (매퍼 insert·select·resultMap 3곳)
- DB: `tb_order.order_type VARCHAR(20) NOT NULL DEFAULT 'PICKUP'` (`db/init/01_schema.sql` 반영)
  - 로컬 DB 는 `ALTER TABLE ... ADD COLUMN ... AFTER payment_method` 후, 주소가 있는 기존 주문 30건을 DELIVERY 로 UPDATE
  - ⚠️ 다른 PC 는 DB 볼륨을 새로 만들면 스키마에 이미 반영돼 있음. 기존 볼륨이면 같은 ALTER 1줄 필요
- 결제수단 값은 앱에서 `ON_SITE`(현장결제)로 보낼 예정 (서버는 문자열 그대로 저장, PG 연동은 추후)
- 주문 상태(배달중·배달 완료 등)는 그대로 둠 → 앱에서만 포장 용어로 표시. 상태 체계 변경은 사장님 화면 작업 때

### 검증
- 스모크 테스트 53 → **58/58 통과**
  - 기존 배달 주문 케이스는 `orderType: DELIVERY` 명시 (금액 7000·9000 그대로)
  - 추가 5건: 포장 주문(주소 없이, 배달비 0 → 6000원) / 잘못된 주문 방식 거부 / 배달 주소 누락 거부 / 포장 주문 상세(orderType=PICKUP·배달비 0·연락처 저장) / 주문 목록 orderType 포함

## 2026-10-26 16:24 손님 흐름 버그 수정 (앱 점검에서 발견) (0b21267, 공지 API 와 묶음)

### 배경
- 손님 앱 전체를 별도 점검(코드 + GET API 호출)해 14건 발견. 그중 백엔드 쪽 수정

### 수정
- **주문 확인 API(`POST /orders/confirm`) 제거** [높음]: 본인 주문인지만 보고 상태 검사 없이 CONFIRMED 로 바꿔, 손님이 취소·완료된 주문도 "주문확인"으로 되살릴 수 있었음. 원래 사장님 기능이라 컨트롤러에서 제거, 서비스 메서드는 사장님 API 만들 때 사장님 권한 + 상태 전이 검사로 다시 씀
- **영업 중인 가게만 주문** [높음]: 가게 존재만 확인하던 것을 `status == OPEN` 아니면 `restaurant_closed`(61001002). 포장 주문이라 닫은 가게 주문은 받을 수 없음 (영업시간 기준 검사는 영업시간 데이터 정비 후)
- **주문 입력 검사**: 수량 `@Max(99)`(노쇼 대비), 가게 요청사항 `@Size(max=100)`, 포장 주문 연락처 필수 + 휴대폰 형식(`^01[0-9]{8,9}$`, `invalid_contact_phone` 63001011), 결제수단 ON_SITE·CARD 만(`invalid_payment_method` 63001012)
- **리뷰 작성 조건**: 받아간(DELIVERED) 본인 주문만 작성 가능(`review_order_not_completed` 64001006). 작성 가능 여부 API 도 주문 존재·본인·상태까지 확인 (전에는 리뷰 중복만 보고 남의 주문에도 true)
- **리뷰 작성자 정보 숨김**: 로그인 없이 보는 가게 리뷰 목록·리뷰 단건 응답에서 작성자 로그인 아이디 제거, 이름은 첫 글자만(예: 김**). 내 리뷰 목록은 그대로
- **주문 목록 "메뉴 N개"**: 메뉴 종류 수(COUNT) → 수량 합(SUM(quantity))

### 검증
- 스모크 테스트 58 → **67/67 통과**
  - 추가: 문 닫은 가게 주문 거부 / 수량 100 거부 / 포장 연락처 누락·형식 거부 / 결제수단 거부 / 주문 확인 API 차단 / 진행 중 주문 리뷰 불가(작성 가능 여부 false + 작성 거부) / 공개 리뷰 목록 userId 없음
  - 리뷰 작성 성공 흐름은 리뷰용 포장 주문을 만들고 **로컬 DB 컨테이너에서 DELIVERED 로 직접 변경**(`mark_order_delivered`, 사장님 API 전까지 임시) 후 작성·중복 거부 확인. DB 에 접근 못 하면 SKIP
  - 테스트 헬퍼 버그 수정: 응답 data 가 false 면 빈 dict 로 바뀌던 것(`or {}`) → None 일 때만 빈 dict
- 보류: 사용자별 진행 중 주문 수 제한 (사장님 작업 때)

## 2026-10-26 16:33 공지사항 API 신규 (0b21267, 18파일 +438 / -31, push 완료)

### 구현
- 테이블 `tb_notice` 신규(제목·내용·상단 고정 is_pinned·공개 is_visible·등록일시·등록자, 목록 인덱스 is_visible+is_pinned+created_datetime) - `db/init/01_schema.sql` 반영, 로컬 DB 는 CREATE TABLE 직접 실행
- 패키지 `domain/notice` 를 개발 컨벤션 구조로(ctrl/dao/dto/svc/impl, 클래스 헤더·DAO 한 줄 주석·SvcImpl Author/Description/Date·매퍼 SQL ID 주석)
  - `GET /notices?page=1&limit=20`: 공개 공지만, 고정 공지 먼저 → 최신순, 목록에는 본문(content) 빼고. limit 1~50 으로 제한
  - `GET /notices/{id}`: 공개 공지 상세, 없거나 비공개면 `notice_not_found`(64002001, 404)
  - 비로그인 조회 허용(application.yml 의 GET 허용 목록에 `/notices/**`)
- 등록·수정 API 는 아직 없음 → 당분간 DB 에 직접 INSERT (관리자 기능 때)
- 테스트 데이터: 고정 1건 + 일반 2건 + 비공개 1건 (`db/test/01_test_data.sql`)

### 검증
- 스모크 테스트 67 → **70/70 통과** (공지 목록: 고정이 맨 위·비공개 제외·본문 없음 / 공지 상세 본문 있음 / 비공개 공지 상세 404)

## 2026-10-26 16:44 환경설정 API - 내 정보 수정·비밀번호 변경·회원탈퇴 (3e7a3ef, 11파일 +341 / -25, push 완료)

### 구현
- `GET /user/me` 를 **DB 조회로 변경**: 전에는 로그인 토큰에 담긴 이름·전화번호를 그대로 돌려줘서, 정보를 수정해도 다시 로그인하기 전까지 옛 값이 보였음
- `PUT /user/me` 내 정보 수정(이름 필수·50자, 전화번호는 비우면 삭제·입력하면 휴대폰 숫자만) → 수정된 정보 반환
- `PUT /user/me/password` 비밀번호 변경: 현재 비밀번호 확인(`invalid_password`) → 새 비밀번호 8~20자(가입 화면과 같은 기준) → **모든 기기 로그아웃**(기존 `authService.logout` = 사용자 전체 토큰 무효화)
- `DELETE /user/me` 회원탈퇴: 비밀번호 확인 → `use_yn = N` + 이름·전화번호 삭제 → 모든 토큰 무효화
  - 주문·리뷰 기록은 가게 정산·통계 때문에 남김(리뷰 작성자는 공개 목록에서 이미 가려짐)
  - **탈퇴한 아이디는 재가입 불가**: 아이디 중복 확인에서 `use_yn = 'Y'` 조건을 빼서 탈퇴 아이디도 사용 중으로 봄 (다른 사람이 같은 아이디로 가입해 이전 기록과 섞이는 것 방지). 로그인은 기존에도 use_yn = Y 만 허용
- 요청 DTO 3개(`UserProfileUpdateRequest`·`PasswordChangeRequest`·`WithdrawRequest`), DAO/매퍼 5개 쿼리, 서비스 4개 메서드(컨벤션 주석)

### 발견·수정한 버그
- **같은 초에 새로 로그인한 토큰도 거부되던 문제**: 사용자 전체 토큰 무효화 시각을 밀리초로 저장하는데 JWT 발급시각(iat)은 초 단위라, 로그아웃(또는 비밀번호 변경) 직후 1초 안에 다시 로그인하면 새 토큰이 "로그아웃 이전 발급"으로 판정됨 → 무효화 시각을 초 단위로 내림해 저장. 대신 무효화한 바로 그 초에 먼저 발급된 토큰은 살아남는데(최대 1초 차이), 실사용에선 영향 없음
- 스모크 테스트도 이 경계에 걸려 로그아웃·비밀번호 변경·탈퇴 확인 앞에 1.1초 대기 추가

### 검증
- 스모크 테스트 70 → **87/87 통과** (설정 17건: 정보 수정·바로 반영·전화번호 형식·이름 빈 값 / 현재 비밀번호 틀림·8자 미만·변경·변경 전 토큰 거부·옛 비밀번호 로그인 거부·새 비밀번호 로그인 / 탈퇴 비밀번호 틀림·탈퇴·탈퇴 후 토큰 거부·로그인 거부·같은 아이디 재가입 거부)
- 메모: 서버는 계정당 refresh 토큰을 하나만 보관해서, 같은 계정으로 다른 곳(스크립트 등)에서 로그인하면 앱의 토큰 갱신이 실패해 로그아웃된다(의도된 동작으로 보임, 에뮬레이터 확인 중 실제로 겪음)

## 2026-10-26 16:53 사장님 1묶음 - 사장님 API (가게 연결·영업 상태·대기시간·주문 단계·품절) (def4655, 18파일 +858 / -23, push 완료)

### 배경
- 서비스 차별점("줄 없는 픽업 + 사장님이 직접 누르는 영업중/품절/대기시간")의 사장님 쪽. 옛날 앱 사장님 화면 6개는 가짜 데이터로만 돌아가고 백엔드에 사장님 API 가 하나도 없었음
- 범위: 이번엔 핵심만(영업 상태·대기시간·주문 처리·품절). 재고·매출 분석 그래프·메뉴 추가/수정/삭제·사장님 가입/가게 등록은 다음

### 구현
- **가게 ↔ 사장님 연결**: `tb_store.owner_id` 컬럼 + 인덱스 (`db/init/01_schema.sql`, 로컬 DB 는 ALTER). 한 사장님 = 가게 하나
- **테스트 사장님 계정**: owner1(붕어빵 하우스)·owner2(호떡 코너), 비밀번호 Owner1234!(BCrypt 해시로 `db/test/01_test_data.sql` 에), 권한 ROLE_OWNER(시드에 이미 정의돼 있던 역할)
- **권한**: `SecurityConfig` 에 `/owner/**` → `hasRole("OWNER")` (손님이 부르면 403 permission_denied). 어느 가게인지는 서비스가 로그인 아이디로 내 가게 ID 를 찾아 모든 쿼리를 그 가게로 한정
- **내 정보(`GET /user/me`)에 `isOwner`·`storeId`** 추가 → 앱이 로그인 후 사장님 화면을 띄울지 판단
- `domain/owner` 신규 (ctrl/dao/dto/svc/impl + `mapper/domain/owner.xml`, 컨벤션 주석)
  - `GET /owner/store` 내 가게 + 오늘 현황(오늘 주문 수·오늘 매출(픽업완료 합)·새 주문·처리 중)
  - `PATCH /owner/store/status` 영업중 OPEN / 잠시 쉼 TEMPORARILY_CLOSED / 영업종료 CLOSED (그 밖 값 `invalid_store_status`). 영업중이 아니면 손님 주문이 거부됨(기존 restaurant_closed)
  - `PATCH /owner/store/wait-time` 대기시간 0~120분 → 손님 화면 "조리 약 N분"(tb_store.delivery_time 재사용, 새 컬럼 없음)
  - `GET /owner/orders?status=PENDING&status=...` 내 가게 주문(최신순, 메뉴 요약 "팥 붕어빵 x2, …"·수량 합·연락처·요청사항)
  - `PATCH /owner/orders/{id}/status` **주문 단계 변경**: 접수→확인→조리→픽업대기→픽업완료 순서로만, 확인 단계까지 거절(CANCELLED, 사유 필수 `reject_reason_required`). 순서 위반 `invalid_order_status_change`. **"지금 상태가 확인한 상태일 때만" UPDATE**(0건이면 거부) → 손님이 같은 순간 취소해도 덮어쓰지 않음. 바뀔 때마다 손님 진행 이력(`ORDER_CONFIRMED`·`ORDER_PREPARING`·`ORDER_DELIVERING`·`ORDER_DELIVERED`·`ORDER_CANCELLED` + 포장 문구, 거절은 사유 포함) 기록. 다른 가게 주문은 "없는 주문"으로 응답
  - `GET /owner/menus`, `PATCH /owner/menus/{id}/sold-out` 품절 켜고 끄기(내 가게 메뉴만, 아니면 menu_not_found) → 손님은 품절 메뉴 주문 불가(기존 menu_sold_out)
- 오류 코드 4개: owner_store_not_found(61001007)·invalid_store_status(61001008)·invalid_order_status_change(63001013)·reject_reason_required(63001014)
- 예전 `confirmOrder` 서비스 메서드·`OrderConfirmRequest/Response` 는 호출하는 곳이 없어 정리 대상으로 주석만 달아 둠 (파일 삭제는 확인 후)

### 검증
- 스모크 테스트 87 → **118/118 통과**
  - 리뷰 흐름의 "주문 완료 처리"를 **로컬 DB 직접 UPDATE(임시 헬퍼) → 사장님 API 로 교체**(확인→조리→픽업대기→픽업완료 + 손님 진행 이력 마지막이 ORDER_DELIVERED)
  - 사장님 시나리오 추가: 내 정보 isOwner·storeId / 손님 사장님 API 403 / 오늘 현황 / 대기시간 25분 → 손님 가게 상세 반영 → 범위 초과 거부 → 원복 / 잠시 쉼 → 손님 주문 거부 → 잘못된 상태 거부 → 영업 재개 / 단계 건너뛰기 거부 / 사유 없는 거절 거부 / 다른 가게 사장님 처리 거부 / 새 주문 목록 / 거절 / 거절한 주문 재처리 거부 / 메뉴 목록 / 품절 → 손님 주문 거부 → 해제 / 남의 가게 메뉴 품절 거부

## 2026-10-26 17:02 스모크 테스트 - 접수 상태 주문 남기지 않기 (76c8c58, push 완료)
- "주문 생성(옵션 추가금)" 주문이 매번 접수(PENDING)로 남아 사장님 화면 새 주문에 쌓이던 문제 → 정리 단계에서 취소. **119/119 통과**
- 앱 사장님 화면 확인 중 발견: 붕어빵 하우스에 이전 실행이 남긴 접수 주문 16건(로컬 DB, 정리 여부 확인 후)

## 2026-10-26 17:09 로컬 테스트 주문 정리
- 붕어빵 하우스에 남아 있던 접수(PENDING) 주문 14건을 사장님 API(owner1)로 거절 처리(사유 "테스트 주문 정리"). 호떡 코너는 0건. smoke1 의 픽업대기 주문 1건은 화면 확인용으로 남김

## 2026-10-26 17:21 사장님 API 2 - 메뉴 추가·수정·삭제, 가게 정보·영업시간 수정 (5ee9d81, 13파일 +597 / -2, push 완료)

### 구현 (`domain/owner`, 모두 로그인한 사장님 가게로 한정)
- `POST /owner/menus` 메뉴 추가 / `PUT /owner/menus/{id}` 수정 / `DELETE /owner/menus/{id}` 삭제
  - 카테고리는 **이름으로** 받아 내 가게에서 찾고, 없으면 새로 만듦(비우면 "기본"). 새 카테고리·메뉴는 맨 뒤 순서(MAX(sort_order)+1)
  - 삭제는 지난 주문·리뷰가 메뉴를 참조해서 **실제로 지우지 않고 is_active = 0** (사장님 목록·손님 메뉴에서 사라짐, 다시 삭제하면 menu_not_found)
  - 이름 50자·설명 200자·가격 0~100만원 검사, 다른 가게 메뉴는 menu_not_found
- `PUT /owner/store` 가게 정보 수정(이름·소개 300자·전화 숫자만·최소 주문금액). 주소·위치는 지도 기능 때(좌표가 함께 바뀌어야 해서)
- `GET /owner/store/hours` 월~일 7개(아직 저장 안 한 요일은 시각 없이) / `PUT /owner/store/hours` 요일별 저장
  - 요일 MONDAY~SUNDAY 중복 없이, 휴무가 아니면 여는·닫는 시각(HH:mm) 필수·같으면 안 됨(invalid_business_hours). 닫는 시각이 더 이르면 자정 넘겨 영업으로 봄
  - `INSERT ... ON DUPLICATE KEY UPDATE`(가게+요일 유니크 키)로 있으면 수정·없으면 추가
- 내 가게 응답에 소개·최소 주문금액, 메뉴 응답에 설명 추가
- **손님 가게 메뉴에서 메뉴 없는 카테고리 제외** (`StoreServiceImpl.getStoreMenu`): 사장님이 카테고리의 메뉴를 다 지우면 손님 화면에 빈 제목만 남던 문제(스모크 테스트에서 발견)

### 검증
- 스모크 테스트 119 → **142/142 통과** (메뉴 추가(새 카테고리)→손님 메뉴에 보임→수정(카테고리 이동)→가격 음수 거부→다른 사장님 수정 거부→삭제→손님 메뉴에서 사라짐→빈 카테고리 안 보임→재삭제 거부 / 가게 정보 수정→손님 상세 반영→전화 형식 거부→원복 / 영업시간 7일→저장(일요일 휴무)→시각 없음·요일 중복·형식 거부)
- 메모: 스모크 테스트가 붕어빵 하우스 영업시간을 월~토 10:00~22:00·일 휴무로 저장해 둠(원래 월요일만 있었음, 손님 가게정보 탭에 7일이 보임)

## 2026-10-26 17:29 검색 개선 (ab19095, push 완료)
- 기존 `GET /stores/search` 는 가게 이름·소개만 찾아서 "붕어빵"으로 붕어빵 가게 일부, "슈크림"으로는 아무것도 안 나옴 → 검색 조건을 `<sql id="searchCondition">` 하나로 모아 **간식 종류 이름(tb_store_category.category_name)·판매 중인 메뉴 이름(is_active = 1)** 도 찾게 함(목록·개수 쿼리 공통)
- 검색어 앞뒤 공백 제거, LIKE 특수문자(% _)를 `!` 로 이스케이프(`StoreSearchRequest.getKeywordLike`, `ESCAPE '!'`) → "%" 검색이 전체 가게를 돌려주던 문제
- 받기만 하고 안 쓰던 `category` 필터 적용, 같은 거리면 가게 ID 순으로 정렬 고정(페이지 넘길 때 순서 흔들림 방지)
- 스모크 테스트 142 → **147/147** (가게 이름·메뉴 이름·간식 종류로 찾기 / % 는 와일드카드 아님 / 없는 검색어 0건)

## 2026-10-26 17:36 사장님 매출 API (0abe835, 10파일 +323 / -1, push 완료)

### 구현
- `GET /owner/sales?period=DAY|WEEK|MONTH` (사장님 가게만) → `OwnerSalesResponse`
  - DAY = 오늘 포함 최근 7일 / WEEK = 이번 주(월요일 시작) 포함 최근 8주 / MONTH = 이번 달 포함 최근 6개월, 그 밖 값은 `invalid_sales_period`(63001015)
  - 매출은 **받아간(DELIVERED) 주문** 금액 합. 날짜별 매출을 한 번에 가져와(`getDailySales`, GROUP BY DATE) 서비스에서 주·달 칸으로 묶음 → 쿼리 1개로 세 기간 처리, **매출 없는 칸도 0** 으로 채워 그래프가 비지 않게
  - 칸 이름: 일별 "10/26(월)"(요일은 Locale.KOREAN), 주별 "10/20~", 월별 "10월"
  - 합계·주문 수(칸 합), 취소 주문 수(`getCancelCount`), 인기 메뉴 상위 5개(`getTopMenus`: 수량 순, 메뉴별 단가×수량 합)
- 테스트 데이터: 붕어빵 하우스의 지난 픽업완료 주문 19건(오늘 기준 1~150일 전, `DATE_SUB(DATE(NOW()), ...)` 라 언제 넣어도 최근 기간에 보임) + 주문 항목 36건 (`db/test/01_test_data.sql`, 로컬 DB 적용)

### 검증
- 스모크 테스트 147 → **152/152** (일·주·월별 칸 수 7·8·6, 합계 = 칸 합, 인기 메뉴 / 잘못된 기간 거부 / 손님 조회 거부)

## 2026-10-26 17:41 master 병합 (PR #2, be4b62c)
- PR #1(백엔드 정리, 14:59 병합) 이후 `refactor/backend-cleanup` 에 쌓인 9개 커밋(포장 주문·검증 강화·공지·환경설정·사장님 API·검색·매출)을 PR #2 로 master 에 병합. 충돌 없음. 이제 GitHub 기본 브랜치(master)에서 최신 백엔드가 보임

## 2026-10-26 17:54 가게 별점 불일치 수정 (미커밋)

### 원인
- 가게 목록·홈·랭킹은 `tb_store_stats`(평균 별점·리뷰 수·주문 수)를 읽는데, 이 표를 갱신하는 코드가 어디에도 없어 테스트 데이터의 임시 숫자(4.5점·리뷰 10개 등)가 그대로 노출됨. 상세 화면은 리뷰 표에서 바로 계산(`/reviews/store/{id}/stats`)하므로 두 화면 별점이 달랐음

### 수정
- `StoreDao.mergeStoreStats(storeId)`: 실제 리뷰(AVG·COUNT)와 받아간(DELIVERED) 주문 수로 통계를 다시 계산해 저장 (INSERT … ON DUPLICATE KEY UPDATE, 통계 행이 없던 가게도 생성 → INNER JOIN 쿼리에서 빠지지 않음)
- 호출 시점: 리뷰 작성·수정·삭제 직후(`ReviewServiceImpl`), 사장님이 주문을 픽업 완료로 바꿀 때(`OwnerSvcImpl.modOrderStatus`, "주문 많은 순" 정렬 기준)
- 테스트 데이터(`db/test/01_test_data.sql`) 끝에 전 가게 통계 재계산 구문 추가, 로컬 DB에도 적용 → 리뷰 없는 가게는 0점·0개로 정직하게 표시

### 검증
- 스모크 테스트 152 → **153/153** (리뷰 작성 뒤 목록 별점·리뷰 수 = 상세 통계)
- DB 확인: 1번 가게 통계 37개·4.97점·픽업완료 33건 = 실제 리뷰·주문 계산값과 일치

## 2026-10-26 18:00 사장님 가입·가게 등록 API (미커밋)

### 구현
- **사장님으로 가입**: `POST /user/register` 에 `isOwner`(true 면 사장님) 추가. 권한 할당 쿼리(`regUserAuthMap`)를 권한 ID를 받도록 바꿔 손님 `ROLE_USER` / 사장님 `ROLE_OWNER` 중 하나 할당 (`BaseConstant.ROLE_OWNER` 추가). 가입 직후 사장님은 가게 없음 → `/user/me` 의 `storeId` null, 사장님 API 는 `owner_store_not_found`(61001007)
- **가게 등록** `POST /owner/store` (`OwnerRequests.StoreRegisterRequest`)
  - 입력: 이름·간식 종류(필수)·소개·전화·주소(필수)·위도/경도(필수, 국내 범위 33~39 / 124~132)·최소 주문금액(선택, 기본 0)
  - **사업자번호는 받지 않음**: 사업자등록 없이 장사하는 노점이 많아 입력 단계에서 제외 (DB 컬럼은 유지)
  - 사장님 한 명당 한 가게 (`owner_store_exists` 61001009), 간식 종류는 앱 홈 카테고리 10종 코드만 허용 (`invalid_store_category` 61001010)
  - 등록 시 같이 만드는 것: 가게(`CLOSED` 로 시작 → 사장님이 영업 시작해야 주문 열림, 대기 10분), 검색용 간식 종류 한글 이름(`tb_store_category`), 기본 영업시간(매일 10:00~21:00), 가게 통계 행(목록 INNER JOIN 에서 빠지지 않게)
  - 저장 DTO `OwnerSaveDtos.StoreSave` (useGeneratedKeys 로 새 가게 ID)

### 검증
- 스모크 테스트 153 → **166/166**: 손님 가입은 사장님 아님 / 사장님 가입 → 가게 없음 상태 / 손님의 가게 등록 거부 / 없는 간식 종류·위치 누락·국외 위치 거부 / 가게 등록(영업 종료·기본 영업시간 7일·"타코야키" 검색 노출) / 두 번째 등록 거부
  - 실제 등록은 고정 계정 `smoke_store_owner` 로 **한 번만** 하고 이후 실행에선 중복 거부만 확인 → 매번 테스트 가게가 손님 목록에 쌓이지 않음 (로컬 DB에 "등록 테스트 가게" 1개 생성됨)
  - 테스트 보정: 새로 등록한 가게는 사진이 없어 목록 사진 검사는 테스트 데이터 매장(1~8번)만, 별점 비교는 DB ROUND 와 같은 사사오입
