-- =====================================================================
-- winter_app 스키마 (MariaDB 11.4)
-- 실제 운영 DB 접근 불가로, MyBatis 매퍼(src/main/resources/mapper/**)와
-- DTO 를 근거로 역설계한 DDL. 매퍼가 참조하는 모든 컬럼을 포함한다.
--   * 실제 FOREIGN KEY 제약은 걸지 않음(INSERT 유연성) → 관계는 주석으로만 표기
--   * "-- 추정" 주석 = 코드만으로 타입/길이/기본값을 확정할 수 없는 컬럼
--   * docker-compose 최초 기동 시 /docker-entrypoint-initdb.d 로 자동 실행
-- =====================================================================

USE winter_app;

-- ---------------------------------------------------------------------
-- 1. 사용자 / 인증
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS tb_user (
    id          VARCHAR(50)  NOT NULL COMMENT '사용자 로그인 아이디(문자열 PK)',
    pw          VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',            -- 추정: BCrypt 길이 고려
    name        VARCHAR(50)  NULL     COMMENT '사용자 이름',                  -- 추정
    tel         VARCHAR(30)  NULL     COMMENT '연락처(전화번호 또는 이메일)', -- 추정
    use_yn      CHAR(1)      NOT NULL DEFAULT 'Y' COMMENT '사용 여부(Y/N)',   -- regUser 가 생략 → 기본값 Y 필요
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시', -- 추정: 매퍼 미참조
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '회원';

CREATE TABLE IF NOT EXISTS tb_user_address (
    idx                   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '주소 PK',
    user_id               VARCHAR(50)   NOT NULL COMMENT '사용자 아이디 (→ tb_user.id)',
    alias                 VARCHAR(50)   NULL     COMMENT '별칭(집/회사/기타)',      -- 추정
    base_address          VARCHAR(255)  NOT NULL COMMENT '기본 주소(도로명/지번)',  -- 추정
    detail_address        VARCHAR(255)  NULL     COMMENT '상세 주소',               -- 추정
    latitude              DECIMAL(10,7) NULL     COMMENT '위도',
    longitude             DECIMAL(10,7) NULL     COMMENT '경도',
    is_default            CHAR(1)       NOT NULL DEFAULT 'N' COMMENT '기본 주소 여부(Y/N) - 코드가 Y/N 문자로 갱신',
    delivery_instructions VARCHAR(500)  NULL     COMMENT '배달 요청사항',           -- 추정
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (idx),
    KEY idx_user_address_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '사용자 배송지 주소';

CREATE TABLE IF NOT EXISTS tb_user_login_history (
    id       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '이력 PK', -- 추정: 매퍼 미참조
    user_id  VARCHAR(50) NOT NULL COMMENT '사용자 아이디 (→ tb_user.id)',
    crte_dt  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '로그인 일시',
    PRIMARY KEY (id),
    KEY idx_login_history_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '사용자 로그인 이력';

-- 사용자 역할 (commonSecurity.xml getLoginUserRole, user.xml regUserAuthMap)
CREATE TABLE IF NOT EXISTS tb_auth (
    auth_id    VARCHAR(50)  NOT NULL COMMENT '권한(역할) 아이디 예) ROLE_ADMIN',
    auth_name  VARCHAR(100) NULL     COMMENT '권한 명', -- 추정: 매퍼 미참조
    PRIMARY KEY (auth_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '권한(역할)';

CREATE TABLE IF NOT EXISTS tb_auth_user_map (
    auth_id  VARCHAR(50) NOT NULL COMMENT '권한 아이디 (→ tb_auth.auth_id)',
    user_id  VARCHAR(50) NOT NULL COMMENT '사용자 아이디 (→ tb_user.id)',
    PRIMARY KEY (auth_id, user_id),
    KEY idx_auth_user_map_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '권한-사용자 매핑';

-- ---------------------------------------------------------------------
-- 2. 매장
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS tb_store (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '매장 PK',
    name              VARCHAR(100)  NOT NULL COMMENT '매장명',
    description       VARCHAR(1000) NULL     COMMENT '매장 설명',              -- 추정
    address           VARCHAR(255)  NULL     COMMENT '매장 주소',
    phone             VARCHAR(30)   NULL     COMMENT '매장 전화번호',
    latitude          DECIMAL(10,7) NULL     COMMENT '위도',
    longitude         DECIMAL(10,7) NULL     COMMENT '경도',
    status            VARCHAR(20)   NOT NULL DEFAULT 'OPEN' COMMENT '상태(OPEN/CLOSED/TEMPORARILY_CLOSED)', -- 추정: 기본값
    image_url         VARCHAR(500)  NULL     COMMENT '[레거시] 대표 이미지 - 조회는 tb_store_image 로 이전, store.xml insert/update 만 아직 기록', -- 추정
    category          VARCHAR(50)   NULL     COMMENT '대표 카테고리(코드 또는 명)', -- 추정
    min_order_amount  INT           NOT NULL DEFAULT 0 COMMENT '최소 주문금액',
    delivery_fee      INT           NOT NULL DEFAULT 0 COMMENT '배달비(0 = 무료배달)',
    delivery_time     INT           NULL     COMMENT '예상 배달시간(분)',
    business_number   VARCHAR(20)   NULL     COMMENT '사업자등록번호',          -- 추정
    is_featured       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '추천 매장 여부',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    KEY idx_store_status (status),
    KEY idx_store_category (category),
    KEY idx_store_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '매장';

CREATE TABLE IF NOT EXISTS tb_store_image (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '이미지 PK',   -- 추정: 매퍼 미참조
    store_id    BIGINT       NOT NULL COMMENT '매장 ID (→ tb_store.id)',
    image_url   VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '노출 순서',        -- 추정: 매퍼 미참조
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시', -- 추정
    PRIMARY KEY (id),
    KEY idx_store_image_store (store_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '매장 이미지(LEFT JOIN 대상 - 매장당 여러 행이면 조회가 중복됨)';

CREATE TABLE IF NOT EXISTS tb_store_stats (
    store_id        BIGINT       NOT NULL COMMENT '매장 ID (→ tb_store.id, 1:1)',
    average_rating  DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '평균 평점', -- 추정
    total_reviews   INT          NOT NULL DEFAULT 0 COMMENT '총 리뷰 수',
    total_orders    INT          NOT NULL DEFAULT 0 COMMENT '총 주문 수',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '집계 갱신일시', -- 추정: 매퍼 미참조
    PRIMARY KEY (store_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '매장 통계(평점/리뷰수/주문수 집계)';

CREATE TABLE IF NOT EXISTS tb_store_hours (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '운영시간 PK',
    store_id     BIGINT      NOT NULL COMMENT '매장 ID (→ tb_store.id)',
    day_of_week  VARCHAR(10) NOT NULL COMMENT '요일(MONDAY ~ SUNDAY)',
    open_time    TIME        NULL     COMMENT '오픈 시각',
    close_time   TIME        NULL     COMMENT '마감 시각',
    is_holiday   TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '휴무 여부',
    PRIMARY KEY (id),
    UNIQUE KEY uk_store_hours_day (store_id, day_of_week) -- 추정: 요일당 1행
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '매장 요일별 운영시간';

CREATE TABLE IF NOT EXISTS tb_store_category (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '매장-카테고리 PK',
    store_id       BIGINT      NOT NULL COMMENT '매장 ID (→ tb_store.id)',
    category_code  VARCHAR(30) NOT NULL COMMENT '카테고리 코드', -- 추정
    category_name  VARCHAR(50) NOT NULL COMMENT '카테고리 명',   -- 추정
    PRIMARY KEY (id),
    KEY idx_store_category_store (store_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '매장 카테고리(다대다 태그)';

-- ---------------------------------------------------------------------
-- 3. 음식 메뉴
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS tb_menu_category (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '메뉴 카테고리 PK',
    store_id     BIGINT       NOT NULL COMMENT '매장 ID (→ tb_store.id)',
    name         VARCHAR(100) NOT NULL COMMENT '카테고리 명',
    description  VARCHAR(500) NULL     COMMENT '카테고리 설명', -- 추정
    sort_order   INT          NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    is_active    TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '사용 여부',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    KEY idx_menu_category_store (store_id, sort_order)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '매장별 음식 메뉴 카테고리';

CREATE TABLE IF NOT EXISTS tb_menus (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '음식 메뉴 PK',
    store_id          BIGINT        NOT NULL COMMENT '매장 ID (→ tb_store.id)',
    category_id       BIGINT        NULL     COMMENT '메뉴 카테고리 ID (→ tb_menu_category.id)',
    name              VARCHAR(100)  NOT NULL COMMENT '메뉴 명',
    description       VARCHAR(1000) NULL     COMMENT '메뉴 설명',       -- 추정
    price             INT           NOT NULL DEFAULT 0 COMMENT '판매가',
    original_price    INT           NULL     COMMENT '할인 전 원가',
    image_url         VARCHAR(500)  NULL     COMMENT '메뉴 이미지 URL',  -- 추정
    is_popular        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '인기 메뉴 여부',
    is_recommended    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '추천 메뉴 여부',
    is_sold_out       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '품절 여부',
    is_active         TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '사용 여부',
    sort_order        INT           NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    calories          INT           NULL     COMMENT '칼로리(kcal)',
    ingredients       VARCHAR(1000) NULL     COMMENT '원재료(검색 대상)', -- 추정
    allergens         VARCHAR(500)  NULL     COMMENT '알레르기 유발 성분', -- 추정
    spice_level       INT           NULL     COMMENT '맵기 단계',         -- 추정: 범위 미상
    preparation_time  INT           NULL     COMMENT '조리 시간(분)',      -- 추정: 단위
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    KEY idx_menus_store (store_id, is_active, sort_order),
    KEY idx_menus_category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '음식 메뉴(장바구니/주문이 참조)';

CREATE TABLE IF NOT EXISTS tb_menu_option_group (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '옵션 그룹 PK',
    menu_id           BIGINT       NOT NULL COMMENT '음식 메뉴 ID (→ tb_menus.id)',
    name              VARCHAR(100) NOT NULL COMMENT '옵션 그룹 명',
    is_required       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '필수 선택 여부',
    max_select_count  INT          NOT NULL DEFAULT 1 COMMENT '최대 선택 수', -- 추정: 기본값
    min_select_count  INT          NOT NULL DEFAULT 0 COMMENT '최소 선택 수', -- 추정: 기본값
    sort_order        INT          NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    KEY idx_option_group_menu (menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '메뉴 옵션 그룹';

CREATE TABLE IF NOT EXISTS tb_menu_option (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '옵션 PK',
    option_group_id  BIGINT       NOT NULL COMMENT '옵션 그룹 ID (→ tb_menu_option_group.id)',
    name             VARCHAR(100) NOT NULL COMMENT '옵션 명',
    price_change     INT          NOT NULL DEFAULT 0 COMMENT '추가(차감) 금액',
    is_available     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '선택 가능 여부',
    sort_order       INT          NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    KEY idx_option_group (option_group_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '메뉴 옵션';

-- ---------------------------------------------------------------------
-- 4. 즐겨찾기 / 장바구니
-- ---------------------------------------------------------------------

-- (user_id, store_id) 에 UNIQUE 를 걸지 않음: 논리삭제(use_yn='N') 후 재등록 시
-- FavoriteServiceImpl.addFavorite 가 restoreFavorite 가 아닌 INSERT 를 하므로 중복 행이 생김.
CREATE TABLE IF NOT EXISTS tb_favorites (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '즐겨찾기 PK',
    user_id     VARCHAR(50) NOT NULL COMMENT '사용자 아이디 (→ tb_user.id)',
    store_id    BIGINT      NOT NULL COMMENT '매장 ID (→ tb_store.id)',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    created_by  VARCHAR(50) NULL     COMMENT '등록자',
    updated_by  VARCHAR(50) NULL     COMMENT '수정자',
    use_yn      CHAR(1)     NOT NULL DEFAULT 'Y' COMMENT '사용 여부(Y/N, 논리삭제)',
    PRIMARY KEY (id),
    KEY idx_favorites_user_store (user_id, store_id, use_yn),
    KEY idx_favorites_store (store_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '매장 즐겨찾기(찜)';

CREATE TABLE IF NOT EXISTS tb_cart (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '장바구니 PK',
    user_id     VARCHAR(50) NOT NULL COMMENT '사용자 아이디 (→ tb_user.id)',
    store_id    BIGINT      NOT NULL COMMENT '매장 ID (→ tb_store.id) - 한 장바구니는 한 매장',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_user (user_id) -- findByUserId 단건 조회 + 다른 매장 담기 시 삭제 후 재생성 → 사용자당 1개
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '장바구니(사용자당 1개)';

CREATE TABLE IF NOT EXISTS tb_cart_item (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '장바구니 항목 PK',
    cart_id          BIGINT       NOT NULL COMMENT '장바구니 ID (→ tb_cart.id)',
    menu_id          BIGINT       NOT NULL COMMENT '음식 메뉴 ID (→ tb_menus.id)',
    quantity         INT          NOT NULL DEFAULT 1 COMMENT '수량',
    total_price      INT          NOT NULL DEFAULT 0 COMMENT '항목 합계 금액(옵션 포함)',
    special_request  VARCHAR(500) NULL     COMMENT '요청사항', -- 추정
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    KEY idx_cart_item_cart (cart_id),
    KEY idx_cart_item_menu (menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '장바구니 항목';

CREATE TABLE IF NOT EXISTS tb_cart_item_option (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '항목 옵션 PK',
    cart_item_id  BIGINT       NOT NULL COMMENT '장바구니 항목 ID (→ tb_cart_item.id)',
    option_id     BIGINT       NOT NULL COMMENT '메뉴 옵션 ID (→ tb_menu_option.id)',
    option_name   VARCHAR(100) NULL     COMMENT '옵션 명(스냅샷)', -- 추정
    option_value  VARCHAR(100) NULL     COMMENT '옵션 값',         -- 추정
    option_price  INT          NOT NULL DEFAULT 0 COMMENT '옵션 금액',
    PRIMARY KEY (id),
    KEY idx_cart_item_option_item (cart_item_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '장바구니 항목 선택 옵션';

-- ---------------------------------------------------------------------
-- 5. 주문 / 리뷰
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS tb_order (
    idx                      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '주문 내부 PK',
    order_id                 VARCHAR(50)   NOT NULL COMMENT '주문 번호(애플리케이션 생성 문자열, 업무 키)', -- 추정: 길이
    store_id                 BIGINT        NOT NULL COMMENT '매장 ID (→ tb_store.id)',
    user_id                  VARCHAR(50)   NOT NULL COMMENT '사용자 아이디 (→ tb_user.id)',
    review_id                BIGINT        NULL     COMMENT '작성된 리뷰 ID (→ tb_review.idx)', -- 추정: 매퍼에서 갱신하지 않음
    process_yn               CHAR(1)       NULL     COMMENT '처리 여부(Y/N)',   -- 추정
    message                  VARCHAR(500)  NULL     COMMENT '처리 메시지(취소 사유 등)', -- 추정
    total_amount             DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '총 결제금액',
    delivery_fee             DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '배달비',
    order_request            VARCHAR(500)  NULL     COMMENT '가게 요청사항',     -- 추정
    status                   VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '주문 상태(PENDING/CONFIRMED/PREPARING/DELIVERING/DELIVERED/CANCELLED)',
    payment_method           VARCHAR(30)   NULL     COMMENT '결제 수단',          -- 추정
    delivery_address         VARCHAR(255)  NULL     COMMENT '배달 주소',
    delivery_detail_address  VARCHAR(255)  NULL     COMMENT '배달 상세 주소',
    delivery_phone           VARCHAR(30)   NULL     COMMENT '배달 연락처',
    delivery_request         VARCHAR(500)  NULL     COMMENT '배달 요청사항',
    created_datetime         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '주문일시',
    updated_datetime         DATETIME      NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    created_by               VARCHAR(50)   NULL     COMMENT '등록자',
    updated_by               VARCHAR(50)   NULL     COMMENT '수정자',
    PRIMARY KEY (idx),
    UNIQUE KEY uk_order_order_id (order_id),
    KEY idx_order_user (user_id, created_datetime),
    KEY idx_order_store (store_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '주문';

CREATE TABLE IF NOT EXISTS tb_order_items (
    order_item_id     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '주문 항목 PK',
    order_id          VARCHAR(50)   NOT NULL COMMENT '주문 번호 (→ tb_order.order_id)',
    menu_id           BIGINT        NOT NULL COMMENT '음식 메뉴 ID (→ tb_menus.id)',
    quantity          INT           NOT NULL DEFAULT 1 COMMENT '수량',
    price             DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '단가',
    special_request   VARCHAR(500)  NULL     COMMENT '요청사항', -- 추정
    created_datetime  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    PRIMARY KEY (order_item_id),
    KEY idx_order_items_order (order_id),
    KEY idx_order_items_menu (menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '주문 항목';

CREATE TABLE IF NOT EXISTS tb_order_history (
    history_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '주문 이력 PK',
    order_id          VARCHAR(50)  NOT NULL COMMENT '주문 번호 (→ tb_order.order_id)',
    status            VARCHAR(20)  NOT NULL COMMENT '변경된 주문 상태',
    description       VARCHAR(500) NULL     COMMENT '이력 설명', -- 추정
    created_datetime  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    created_by        VARCHAR(50)  NULL     COMMENT '등록자',
    PRIMARY KEY (history_id),
    KEY idx_order_history_order (order_id, created_datetime)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '주문 상태 변경 이력';

CREATE TABLE IF NOT EXISTS tb_review (
    idx                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '리뷰 PK',
    store_id           BIGINT        NOT NULL COMMENT '매장 ID (→ tb_store.id)',
    user_id            VARCHAR(50)   NOT NULL COMMENT '작성자 아이디 (→ tb_user.id)', -- 추정: 매퍼의 CAST(... AS CHAR) 는 과거 숫자형 흔적
    order_order_id     BIGINT        NULL     COMMENT '주문 참조 ID - DTO 가 Long (tb_order.order_id 는 문자열, tb_order.idx 와 대응하는지 확인 필요)', -- 추정
    rating             INT           NOT NULL COMMENT '평점(1~5)',
    images             VARCHAR(2000) NULL     COMMENT '리뷰 이미지(문자열, 구분자/JSON 형식 미상)', -- 추정
    content            TEXT          NULL     COMMENT '리뷰 내용',
    created_datetime   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    modified_datetime  DATETIME      NULL     COMMENT '수정일시',
    PRIMARY KEY (idx),
    KEY idx_review_store (store_id, created_datetime),
    KEY idx_review_user (user_id, created_datetime),
    KEY idx_review_user_order (user_id, order_order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '매장 리뷰';
