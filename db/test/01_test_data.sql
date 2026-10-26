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
