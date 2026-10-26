-- =====================================================================
-- winter_app 기본 코드 데이터 (01_schema.sql 다음에 실행)
-- 회원가입 시 regUserAuthMap 이 tb_auth_user_map 에 역할을 넣고,
-- 로그인 시 getLoginUserRole 이 tb_auth 와 INNER JOIN 하므로 역할 행이 있어야 한다.
-- =====================================================================

USE winter_app;

INSERT IGNORE INTO tb_auth (auth_id, auth_name) VALUES
    ('ROLE_ADMIN', '관리자'),
    ('ROLE_USER',  '손님'),
    ('ROLE_OWNER', '사장님');
