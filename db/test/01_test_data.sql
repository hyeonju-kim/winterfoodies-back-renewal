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
