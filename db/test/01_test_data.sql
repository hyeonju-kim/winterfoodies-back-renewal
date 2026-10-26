-- =====================================================================
-- 스모크 테스트용 데이터 (로컬 DB 전용, 운영에 넣지 말 것)
-- 실행: docker exec -i winter-mariadb mariadb -uroot -p"$DB_ROOT_PASSWORD" < db/test/01_test_data.sql
-- 여러 번 실행해도 되도록 고정 id + INSERT IGNORE 사용
-- =====================================================================

USE winter_app;

-- 매장 2개 (1번: 메뉴·옵션·운영시간까지 있는 기본 매장)
INSERT IGNORE INTO tb_store (id, name, description, address, phone, latitude, longitude, status, category, min_order_amount, delivery_fee, delivery_time) VALUES
    (1, '붕어빵 하우스', '테스트 매장', '서울 강남구 1', '0212345678', 37.4980000, 127.0276000, 'OPEN', 'BUNGEOPPANG', 5000, 1000, 20),
    (2, '호떡 코너',     '테스트 매장', '서울 서초구 2', '0287654321', 37.4910000, 127.0070000, 'OPEN', 'HOTTEOK',     3000,    0, 15);

-- 1번 매장은 이미지 2장 (대표 이미지 = sort_order 가 가장 작은 store1.png)
INSERT IGNORE INTO tb_store_image (id, store_id, image_url, sort_order) VALUES
    (1, 1, 'https://example.com/store1.png',        1),
    (2, 2, 'https://example.com/store2.png',        1),
    (3, 1, 'https://example.com/store1_second.png', 2);

INSERT IGNORE INTO tb_store_stats (store_id, average_rating, total_reviews, total_orders) VALUES
    (1, 4.50, 10, 100),
    (2, 4.00,  3,  20);

INSERT IGNORE INTO tb_store_hours (id, store_id, day_of_week, open_time, close_time) VALUES
    (1, 1, 'MONDAY', '10:00:00', '22:00:00');

INSERT IGNORE INTO tb_store_category (id, store_id, category_code, category_name) VALUES
    (1, 1, 'BUNGEOPPANG', '붕어빵');

-- 1번 매장 메뉴: 카테고리 1 / 메뉴 2 / 옵션 그룹 1 / 옵션 1
INSERT IGNORE INTO tb_menu_category (id, store_id, name, sort_order) VALUES
    (1, 1, '기본', 1);

INSERT IGNORE INTO tb_menus (id, store_id, category_id, name, description, price) VALUES
    (1, 1, 1, '팥 붕어빵',     '3개', 3000),
    (2, 1, 1, '슈크림 붕어빵', '3개', 3500);

INSERT IGNORE INTO tb_menu_option_group (id, menu_id, name, is_required) VALUES
    (1, 1, '추가', 0);

INSERT IGNORE INTO tb_menu_option (id, option_group_id, name, price_change) VALUES
    (1, 1, '1개 추가', 1000);

-- 홈 화면 확인용 매장 6개 (강남역 주변, 카테고리 분산)
INSERT IGNORE INTO tb_store (id, name, description, address, phone, latitude, longitude, status, category, min_order_amount, delivery_fee, delivery_time) VALUES
    (3, '역삼 어묵포차',     '뜨끈한 국물 어묵',       '서울 강남구 역삼동 3',   '0211112222', 37.5000000, 127.0360000, 'OPEN',   'EOMUK',       4000, 0, 10),
    (4, '논현 군밤할매',     '직화 군밤·군고구마',     '서울 강남구 논현동 4',   '0233334444', 37.5085000, 127.0230000, 'OPEN',   'GUNBAM',      3000, 0, 5),
    (5, '신논현 고구마굽는집', '꿀고구마 전문',         '서울 강남구 논현동 5',   '0255556666', 37.5047000, 127.0250000, 'OPEN',   'GUNGOGUMA',   5000, 0, 15),
    (6, '강남역 계란빵',     '반숙 계란빵',             '서울 강남구 역삼동 6',   '0277778888', 37.4985000, 127.0290000, 'OPEN',   'GYERANPPANG', 3000, 0, 10),
    (7, '테헤란 타코야키',   '문어 듬뿍 타코야키',     '서울 강남구 역삼동 7',   '0299990000', 37.5010000, 127.0390000, 'OPEN',   'TAKOYAKI',    6000, 0, 15),
    (8, '양재 호두과자',     '저녁에만 여는 호두과자', '서울 서초구 양재동 8',   '0212121212', 37.4840000, 127.0340000, 'CLOSED', 'HODUGWAJA',   5000, 0, 10);

INSERT IGNORE INTO tb_store_image (id, store_id, image_url, sort_order) VALUES
    (4, 3, 'https://example.com/store3.png', 1), (5, 4, 'https://example.com/store4.png', 1), (6, 5, 'https://example.com/store5.png', 1),
    (7, 6, 'https://example.com/store6.png', 1), (8, 7, 'https://example.com/store7.png', 1), (9, 8, 'https://example.com/store8.png', 1);

INSERT IGNORE INTO tb_store_stats (store_id, average_rating, total_reviews, total_orders) VALUES
    (3, 4.70, 25, 180), (4, 4.90, 40, 320), (5, 4.30, 8, 60), (6, 4.60, 15, 140), (7, 4.80, 31, 260), (8, 4.20, 5, 30);

INSERT IGNORE INTO tb_menu_category (id, store_id, name, sort_order) VALUES
    (3, 3, '어묵', 1), (4, 4, '군밤', 1), (5, 5, '고구마', 1), (6, 6, '계란빵', 1), (7, 7, '타코야키', 1), (8, 8, '호두과자', 1);

INSERT IGNORE INTO tb_menus (id, store_id, category_id, name, description, price) VALUES
    (3, 3, 3, '어묵 꼬치', '1개', 1000),
    (4, 3, 3, '떡꼬치', '1개', 1500),
    (5, 4, 4, '군밤', '1봉지', 3000),
    (6, 5, 5, '꿀고구마', '3개', 5000),
    (7, 6, 6, '계란빵', '1개', 1500),
    (8, 7, 7, '타코야키', '6알', 4000),
    (9, 7, 7, '타코야키', '10알', 6000),
    (10, 8, 8, '호두과자', '10개', 5000);

-- 공지사항 (상단 고정 1건 + 일반 2건 + 비공개 1건)
INSERT IGNORE INTO tb_notice (idx, title, content, is_pinned, is_visible, created_datetime, created_by) VALUES
    (1, '윈터푸디스는 포장 주문 전용입니다', '윈터푸디스는 가게에서 직접 받아가는 포장 주문만 받습니다.\n결제는 받을 때 가게에서 현금·카드·계좌이체로 해 주세요.', 'Y', 'Y', '2026-10-20 10:00:00', 'admin'),
    (2, '겨울 간식 시즌 오픈', '붕어빵·호떡·군고구마 가게가 하나둘 문을 열고 있어요.\n가까운 가게를 찜해 두면 찜 탭에서 모아볼 수 있습니다.', 'N', 'Y', '2026-10-22 09:00:00', 'admin'),
    (3, '리뷰 작성 안내', '받아간 주문에 한해 주문내역 > 주문 상세에서 리뷰를 쓸 수 있어요.', 'N', 'Y', '2026-10-25 18:00:00', 'admin'),
    (4, '[비공개] 점검 예정 공지 초안', '아직 공개하지 않은 공지입니다.', 'N', 'N', '2026-10-26 08:00:00', 'admin');

-- 사장님 계정 2개 (비밀번호 Owner1234!, BCrypt) - owner1 은 1번 매장, owner2 는 2번 매장 사장님
INSERT IGNORE INTO tb_user (id, pw, name, tel) VALUES
    ('owner1', '$2a$10$iwSPjPUYnxU1SQQCPCcD7uDHOTDUZ/bhBQ7yAp3ON4M46dDHnJNu6', '붕어빵 사장님', '01090001111'),
    ('owner2', '$2a$10$iwSPjPUYnxU1SQQCPCcD7uDHOTDUZ/bhBQ7yAp3ON4M46dDHnJNu6', '호떡 사장님',   '01090002222');
INSERT IGNORE INTO tb_auth_user_map (auth_id, user_id) VALUES ('ROLE_OWNER', 'owner1'), ('ROLE_OWNER', 'owner2');
UPDATE tb_store SET owner_id = 'owner1' WHERE id = 1;
UPDATE tb_store SET owner_id = 'owner2' WHERE id = 2;

-- 매출 화면 확인용: 붕어빵 하우스의 지난 픽업완료 주문 (오늘 기준 1~150일 전, 다시 실행해도 그대로)
INSERT IGNORE INTO tb_order (order_id, store_id, user_id, total_amount, delivery_fee, status, payment_method, order_type, delivery_phone, created_datetime) VALUES
    ('TESTSALES01', 1, 'smoke1', 9500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 1 DAY) + INTERVAL 13 HOUR),
    ('TESTSALES02', 1, 'smoke1', 10000, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 1 DAY) + INTERVAL 14 HOUR),
    ('TESTSALES03', 1, 'smoke1', 9000, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 2 DAY) + INTERVAL 15 HOUR),
    ('TESTSALES04', 1, 'smoke1', 13000, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 3 DAY) + INTERVAL 16 HOUR),
    ('TESTSALES05', 1, 'smoke1', 6500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 4 DAY) + INTERVAL 17 HOUR),
    ('TESTSALES06', 1, 'smoke1', 15500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 5 DAY) + INTERVAL 18 HOUR),
    ('TESTSALES07', 1, 'smoke1', 16500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 6 DAY) + INTERVAL 19 HOUR),
    ('TESTSALES08', 1, 'smoke1', 12500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 9 DAY) + INTERVAL 12 HOUR),
    ('TESTSALES09', 1, 'smoke1', 13000, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 12 DAY) + INTERVAL 13 HOUR),
    ('TESTSALES10', 1, 'smoke1', 15000, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 16 DAY) + INTERVAL 14 HOUR),
    ('TESTSALES11', 1, 'smoke1', 13500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 20 DAY) + INTERVAL 15 HOUR),
    ('TESTSALES12', 1, 'smoke1', 9500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 27 DAY) + INTERVAL 16 HOUR),
    ('TESTSALES13', 1, 'smoke1', 16000, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 35 DAY) + INTERVAL 17 HOUR),
    ('TESTSALES14', 1, 'smoke1', 15500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 45 DAY) + INTERVAL 18 HOUR),
    ('TESTSALES15', 1, 'smoke1', 13000, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 60 DAY) + INTERVAL 19 HOUR),
    ('TESTSALES16', 1, 'smoke1', 19500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 75 DAY) + INTERVAL 12 HOUR),
    ('TESTSALES17', 1, 'smoke1', 17000, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 95 DAY) + INTERVAL 13 HOUR),
    ('TESTSALES18', 1, 'smoke1', 18500, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 120 DAY) + INTERVAL 14 HOUR),
    ('TESTSALES19', 1, 'smoke1', 13000, 0, 'DELIVERED', 'ON_SITE', 'PICKUP', '01077778888', DATE_SUB(DATE(NOW()), INTERVAL 150 DAY) + INTERVAL 15 HOUR);
INSERT INTO tb_order_items (order_id, menu_id, quantity, price)
SELECT v.order_id, v.menu_id, v.quantity, v.price
  FROM (SELECT 'x' AS order_id, 0 AS menu_id, 0 AS quantity, 0 AS price
        UNION ALL SELECT 'TESTSALES01', 1, 2, 3000
        UNION ALL SELECT 'TESTSALES01', 2, 1, 3500
        UNION ALL SELECT 'TESTSALES02', 1, 1, 3000
        UNION ALL SELECT 'TESTSALES02', 2, 2, 3500
        UNION ALL SELECT 'TESTSALES03', 1, 3, 3000
        UNION ALL SELECT 'TESTSALES04', 1, 2, 3000
        UNION ALL SELECT 'TESTSALES04', 2, 2, 3500
        UNION ALL SELECT 'TESTSALES05', 1, 1, 3000
        UNION ALL SELECT 'TESTSALES05', 2, 1, 3500
        UNION ALL SELECT 'TESTSALES06', 1, 4, 3000
        UNION ALL SELECT 'TESTSALES06', 2, 1, 3500
        UNION ALL SELECT 'TESTSALES07', 1, 2, 3000
        UNION ALL SELECT 'TESTSALES07', 2, 3, 3500
        UNION ALL SELECT 'TESTSALES08', 1, 3, 3000
        UNION ALL SELECT 'TESTSALES08', 2, 1, 3500
        UNION ALL SELECT 'TESTSALES09', 1, 2, 3000
        UNION ALL SELECT 'TESTSALES09', 2, 2, 3500
        UNION ALL SELECT 'TESTSALES10', 1, 5, 3000
        UNION ALL SELECT 'TESTSALES11', 1, 1, 3000
        UNION ALL SELECT 'TESTSALES11', 2, 3, 3500
        UNION ALL SELECT 'TESTSALES12', 1, 2, 3000
        UNION ALL SELECT 'TESTSALES12', 2, 1, 3500
        UNION ALL SELECT 'TESTSALES13', 1, 3, 3000
        UNION ALL SELECT 'TESTSALES13', 2, 2, 3500
        UNION ALL SELECT 'TESTSALES14', 1, 4, 3000
        UNION ALL SELECT 'TESTSALES14', 2, 1, 3500
        UNION ALL SELECT 'TESTSALES15', 1, 2, 3000
        UNION ALL SELECT 'TESTSALES15', 2, 2, 3500
        UNION ALL SELECT 'TESTSALES16', 1, 3, 3000
        UNION ALL SELECT 'TESTSALES16', 2, 3, 3500
        UNION ALL SELECT 'TESTSALES17', 1, 1, 3000
        UNION ALL SELECT 'TESTSALES17', 2, 4, 3500
        UNION ALL SELECT 'TESTSALES18', 1, 5, 3000
        UNION ALL SELECT 'TESTSALES18', 2, 1, 3500
        UNION ALL SELECT 'TESTSALES19', 1, 2, 3000
        UNION ALL SELECT 'TESTSALES19', 2, 2, 3500
       ) v
 WHERE v.order_id != 'x'
   AND NOT EXISTS (SELECT 1 FROM tb_order_items oi WHERE oi.order_id = v.order_id AND oi.menu_id = v.menu_id);
