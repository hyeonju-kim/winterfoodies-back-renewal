# -*- coding: utf-8 -*-
"""
API 스모크(회귀) 테스트 - 주요 API 를 실제 서버에 순서대로 호출해 정상 응답인지 확인한다.

사전 준비
  1) docker compose up -d            (MariaDB + Redis)
  2) 테스트 데이터 적재                db/test/01_test_data.sql
  3) 서버 실행                        ./mvnw spring-boot:run

실행
  python scripts/smoke_test.py                         # 기본 http://localhost:8085
  python scripts/smoke_test.py --base-url http://localhost:8086

판정
  - 응답 code 가 기대값(기본 20000000=성공)이면 OK
  - 하나라도 실패하면 종료 코드 1
"""
import argparse
import json
import subprocess
import sys
import time
import urllib.error
import urllib.request

SUCCESS = '20000000'
UNAUTHORIZED = '40001001'
NOT_FOUND_URL = '30003001'
TOKEN_WRONG = '40001007'
TOKEN_BLACKLISTED = '40001008'
TOKEN_REFRESH_EXPIRE = '40001009'
NOT_SUPPORTED_CODE = '30003003'
METHOD_NOT_ALLOWED = '30003002'
REVIEW_ALREADY_EXISTS = '64001002'
MENU_NOT_FOUND = '62001001'
MENU_OPTION_NOT_FOUND = '62002001'
MINIMUM_ORDER_AMOUNT = '63001007'
INVALID_ORDER_TYPE = '63001009'
DELIVERY_ADDRESS_REQUIRED = '63001010'
INVALID_CONTACT_PHONE = '63001011'
INVALID_PAYMENT_METHOD = '63001012'
RESTAURANT_CLOSED = '61001002'
INVALID_INPUT = '30002003'
REVIEW_ORDER_NOT_COMPLETED = '64001006'
NOTICE_NOT_FOUND = '64002001'
INVALID_PASSWORD = '60001004'
USER_NOT_FOUND = '60001001'
DUPLICATED = '41002001'
HIDDEN_NOTICE_ID = 4  # 테스트 데이터의 비공개 공지
CLOSED_STORE_ID = 8  # 테스트 데이터의 영업 종료(CLOSED) 매장


class SmokeTest:
    def __init__(self, base_url):
        self.base_url = base_url.rstrip('/')
        self.results = []

    def call(self, label, method, path, body=None, token=None, expected_code=SUCCESS, check=None):
        """API 를 호출하고 응답 code 를 기대값과 비교해 기록한다. check 가 있으면 응답 data 도 검사한다"""
        data = json.dumps(body).encode('utf-8') if body is not None else None
        request = urllib.request.Request(self.base_url + path, data=data, method=method)
        request.add_header('Content-Type', 'application/json')
        if token:
            request.add_header('Authorization', 'Bearer ' + token)
        try:
            with urllib.request.urlopen(request) as response:
                http_status = response.status
                text = response.read().decode('utf-8')
        except urllib.error.HTTPError as error:
            http_status = error.code
            text = error.read().decode('utf-8')
        except urllib.error.URLError as error:
            print('서버에 연결할 수 없습니다: %s (%s)' % (self.base_url, error.reason))
            sys.exit(2)

        try:
            payload = json.loads(text)
            code = payload.get('code')
            description = payload.get('desc', '')
        except ValueError:
            payload = {}
            code = None
            description = 'JSON 아님: ' + text[:80]

        # false·0 같은 값은 그대로 두고 없을 때만 빈 dict
        data = payload.get('data')
        if data is None:
            data = {}
        is_passed = code == expected_code
        # 응답 본문의 status 와 실제 HTTP 상태 코드가 같아야 한다 (에러도 200 으로 내려가던 문제 방지)
        if is_passed and payload.get('status') is not None and payload.get('status') != http_status:
            is_passed = False
            description = 'HTTP 상태 %s != 본문 status %s' % (http_status, payload.get('status'))
        if is_passed and check is not None:
            message = check(data)
            if message:
                is_passed = False
                description = '응답 검사 실패: ' + message
        self.results.append((label, is_passed, code, description))
        return data

    def run(self, user_id):
        password = 'Test1234!'

        # 인증
        self.call('회원가입', 'POST', '/user/register',
                  {'id': user_id, 'pw': password, 'name': 'smoke', 'tel': '01011112222'})
        tokens = self.call('로그인', 'POST', '/login', {'username': user_id, 'password': password})
        access_token = tokens.get('accessToken')
        refresh_token = tokens.get('refreshToken')
        self.call('로그인 실패(비밀번호 오류)', 'POST', '/login',
                  {'username': user_id, 'password': 'wrong'}, expected_code='60001004')
        self.call('내 정보', 'GET', '/user/me', token=access_token)

        # 가게·메뉴 조회 (비로그인)
        self.call('카테고리', 'GET', '/categories')
        self.call('없는 URL 404', 'GET', '/no-such-path', token=access_token, expected_code=NOT_FOUND_URL)
        self.call('가게 목록', 'GET', '/stores?lat=37.5&lng=127.0&page=0&size=10', check=check_store_list)
        first_page = self.call('가게 목록 1페이지(1건)', 'GET', '/stores?lat=37.5&lng=127.0&page=0&size=1',
                               check=lambda data: None if len(data.get('stores', [])) == 1 else '1건이 아님')
        self.call('가게 목록 2페이지(다른 매장)', 'GET', '/stores?lat=37.5&lng=127.0&page=1&size=1',
                  check=lambda data: check_next_page(first_page, data))
        self.call('가게 검색', 'GET', '/stores/search?keyword=%EB%B6%95&lat=37.5&lng=127.0')
        self.call('가게 상세(대표 이미지)', 'GET', '/stores/1?lat=37.5&lng=127.0', check=check_store_detail_image)
        self.call('가게 메뉴', 'GET', '/stores/1/menu')
        self.call('가게 랭킹(type 미지정)', 'GET', '/stores/rankings')
        self.call('가게 랭킹(popular)', 'GET', '/stores/rankings?type=popular')
        self.call('가게 랭킹(없는 종류)', 'GET', '/stores/rankings?type=unknown', expected_code=NOT_SUPPORTED_CODE)
        self.call('메뉴 상세', 'GET', '/menu/1')
        self.call('홈 피드(비로그인)', 'GET', '/home/feed?lat=37.5&lng=127.0')

        # 찜
        self.call('찜 추가', 'POST', '/favorites', {'storeId': 1}, token=access_token)
        self.call('찜 목록', 'GET', '/favorites', token=access_token)
        self.call('찜 확인', 'GET', '/favorites/check/1', token=access_token)
        self.call('찜 해제 후', 'DELETE', '/favorites/1', token=access_token)
        self.call('찜 다시 추가', 'POST', '/favorites', {'storeId': 1}, token=access_token)
        self.call('찜 다시 추가 후 목록(1건)', 'GET', '/favorites', token=access_token,
                  check=lambda data: None if data.get('totalCount') == 1 else 'totalCount=%s' % data.get('totalCount'))

        # 배송지
        self.call('배송지 등록', 'POST', '/addresses',
                  {'alias': 'home', 'baseAddress': '서울 강남구 1', 'detailAddress': '101',
                   'latitude': 37.49, 'longitude': 127.02, 'isDefault': 'Y'}, token=access_token)
        self.call('배송지 목록', 'GET', '/addresses', token=access_token)

        # 장바구니 → 주문
        self.call('장바구니 담기', 'POST', '/cart/items',
                  {'menuId': 1, 'storeId': 1, 'quantity': 2, 'options': [{'optionId': 1}]}, token=access_token)
        self.call('장바구니 조회', 'GET', '/cart', token=access_token)
        delivery_address = {'address': '서울 강남구 1', 'detailAddress': '101', 'phone': '01011112222'}
        # 클라이언트가 금액을 조작해 보내도 서버 계산 금액(팥 붕어빵 3000 x 2 + 배달비 1000 = 7000)으로 저장돼야 한다
        order = self.call('주문 생성(금액 조작 무시)', 'POST', '/orders',
                          {'storeId': 1, 'items': [{'menuId': 1, 'quantity': 2, 'price': 1}],
                           'orderType': 'DELIVERY', 'deliveryAddress': delivery_address,
                           'paymentMethod': 'CARD', 'totalAmount': 100, 'deliveryFee': 0}, token=access_token,
                          check=lambda data: check_amount(data, 7000))
        order_id = order.get('orderId')
        # 옵션 추가금 반영: (3000 + 1000) x 2 + 배달비 1000 = 9000
        self.call('주문 생성(옵션 추가금)', 'POST', '/orders',
                  {'storeId': 1, 'items': [{'menuId': 1, 'quantity': 2, 'options': [{'optionId': 1}]}],
                   'orderType': 'DELIVERY', 'deliveryAddress': delivery_address, 'paymentMethod': 'CARD'}, token=access_token,
                  check=lambda data: check_amount(data, 9000))
        self.call('다른 매장 메뉴 주문 거부', 'POST', '/orders',
                  {'storeId': 2, 'items': [{'menuId': 1, 'quantity': 1}],
                   'orderType': 'DELIVERY', 'deliveryAddress': delivery_address, 'paymentMethod': 'CARD'}, token=access_token,
                  expected_code=MENU_NOT_FOUND)
        self.call('없는 옵션 주문 거부', 'POST', '/orders',
                  {'storeId': 1, 'items': [{'menuId': 2, 'quantity': 2, 'options': [{'optionId': 1}]}],
                   'orderType': 'DELIVERY', 'deliveryAddress': delivery_address, 'paymentMethod': 'CARD'}, token=access_token,
                  expected_code=MENU_OPTION_NOT_FOUND)
        self.call('최소 주문 금액 미달 거부', 'POST', '/orders',
                  {'storeId': 1, 'items': [{'menuId': 1, 'quantity': 1}],
                   'orderType': 'DELIVERY', 'deliveryAddress': delivery_address, 'paymentMethod': 'CARD'}, token=access_token,
                  expected_code=MINIMUM_ORDER_AMOUNT)
        # 포장 주문(기본): 배송 주소 없이 연락처만, 배달비 0 → 3000 x 2 = 6000
        pickup_order = self.call('포장 주문 생성(주소 없이, 배달비 0)', 'POST', '/orders',
                                 {'storeId': 1, 'items': [{'menuId': 1, 'quantity': 2}],
                                  'contactPhone': '01011112222', 'paymentMethod': 'ON_SITE'}, token=access_token,
                                 check=lambda data: check_amount(data, 6000))
        pickup_order_id = pickup_order.get('orderId')
        self.call('잘못된 주문 방식 거부', 'POST', '/orders',
                  {'storeId': 1, 'items': [{'menuId': 1, 'quantity': 2}], 'orderType': 'DRONE',
                   'paymentMethod': 'ON_SITE'}, token=access_token, expected_code=INVALID_ORDER_TYPE)
        self.call('배달 주문 주소 누락 거부', 'POST', '/orders',
                  {'storeId': 1, 'items': [{'menuId': 1, 'quantity': 2}], 'orderType': 'DELIVERY',
                   'paymentMethod': 'CARD'}, token=access_token, expected_code=DELIVERY_ADDRESS_REQUIRED)
        if pickup_order_id:
            self.call('포장 주문 상세(방식·배달비·연락처)', 'GET', '/orders/%s' % pickup_order_id, token=access_token,
                      check=lambda data: None if (data.get('orderType') == 'PICKUP' and float(data.get('deliveryFee')) == 0
                                                  and data.get('deliveryInfo', {}).get('phone') == '01011112222')
                      else 'orderType=%s deliveryFee=%s deliveryInfo=%s' % (data.get('orderType'), data.get('deliveryFee'),
                                                                              data.get('deliveryInfo')))
            self.call('포장 주문 취소', 'PUT', '/orders/%s/cancel' % pickup_order_id, {'reason': 'test'}, token=access_token)
        pickup_body = {'storeId': 1, 'items': [{'menuId': 1, 'quantity': 2}],
                       'contactPhone': '01011112222', 'paymentMethod': 'ON_SITE'}
        self.call('문 닫은 가게 주문 거부', 'POST', '/orders', dict(pickup_body, storeId=CLOSED_STORE_ID),
                  token=access_token, expected_code=RESTAURANT_CLOSED)
        self.call('수량 99개 초과 거부', 'POST', '/orders',
                  dict(pickup_body, items=[{'menuId': 1, 'quantity': 100}]), token=access_token,
                  expected_code=INVALID_INPUT)
        self.call('포장 연락처 누락 거부', 'POST', '/orders', dict(pickup_body, contactPhone=None),
                  token=access_token, expected_code=INVALID_CONTACT_PHONE)
        self.call('포장 연락처 형식 거부', 'POST', '/orders', dict(pickup_body, contactPhone='010-1111-2222'),
                  token=access_token, expected_code=INVALID_CONTACT_PHONE)
        self.call('지원하지 않는 결제수단 거부', 'POST', '/orders', dict(pickup_body, paymentMethod='BITCOIN'),
                  token=access_token, expected_code=INVALID_PAYMENT_METHOD)
        self.call('주문 확인 API 손님 차단(제거됨)', 'POST', '/orders/confirm', {'orderId': order_id},
                  token=access_token, expected_code=METHOD_NOT_ALLOWED)
        self.call('주문 목록', 'GET', '/orders', token=access_token,
                  check=lambda data: None if all('orderType' in order for order in data.get('dataList', []))
                  else 'orderType 누락')
        if order_id:
            self.call('주문 상세(저장 금액·결제수단)', 'GET', '/orders/%s' % order_id, token=access_token,
                      check=lambda data: check_amount(data, 7000) or
                      (None if data.get('paymentMethod') == 'CARD' else 'paymentMethod=%s' % data.get('paymentMethod')))
            self.call('주문 추적', 'GET', '/orders/%s/tracking' % order_id, token=access_token)

        # 리뷰 - 받아간(DELIVERED) 주문만 가능. 아직 사장님 API 가 없어 완료 처리는 DB 로 직접 한다
        if order_id:
            self.call('진행 중 주문 리뷰 작성 불가', 'GET', '/reviews/order/%s/writable' % order_id, token=access_token,
                      check=lambda data: None if data is False else '진행 중 주문은 false 여야 함')
            self.call('진행 중 주문 리뷰 거부', 'POST', '/reviews',
                      {'storeId': 1, 'orderOrderId': order_id, 'rating': 5, 'content': 'early'}, token=access_token,
                      expected_code=REVIEW_ORDER_NOT_COMPLETED)
        review_order = self.call('리뷰용 포장 주문 생성', 'POST', '/orders', pickup_body, token=access_token)
        review_order_id = review_order.get('orderId')
        if review_order_id and mark_order_delivered(review_order_id):
            self.call('받아간 주문 리뷰 작성 가능', 'GET', '/reviews/order/%s/writable' % review_order_id,
                      token=access_token, check=lambda data: None if data is True else '작성 가능해야 함')
            self.call('리뷰 작성', 'POST', '/reviews',
                      {'storeId': 1, 'orderOrderId': review_order_id, 'rating': 5, 'content': 'good'},
                      token=access_token)
            self.call('같은 주문 리뷰 중복 거부', 'POST', '/reviews',
                      {'storeId': 1, 'orderOrderId': review_order_id, 'rating': 4, 'content': 'again'},
                      token=access_token, expected_code=REVIEW_ALREADY_EXISTS)
        else:
            print('SKIP  리뷰 작성 성공 흐름 (DB 컨테이너에 접근할 수 없어 주문 완료 처리를 못 함)')
        self.call('가게 리뷰 목록(작성자 아이디 숨김)', 'GET', '/reviews/store/1',
                  check=lambda data: None if all(review.get('userId') is None for review in data.get('dataList', []))
                  else '공개 목록에 userId 노출')
        self.call('내 리뷰', 'GET', '/reviews/my', token=access_token)
        self.call('리뷰 통계', 'GET', '/reviews/store/1/stats')

        # 공지사항 (비로그인 조회)
        self.call('공지 목록(고정 먼저·비공개 제외)', 'GET', '/notices',
                  check=lambda data: None if (data.get('dataList') and data['dataList'][0].get('isPinned') == 'Y'
                                              and all(notice.get('idx') != HIDDEN_NOTICE_ID for notice in data['dataList'])
                                              and all(notice.get('content') is None for notice in data['dataList']))
                  else '고정 공지가 맨 위가 아니거나 비공개·본문 포함')
        self.call('공지 상세', 'GET', '/notices/1', check=lambda data: None if data.get('content') else '본문 없음')
        self.call('비공개 공지 상세 거부', 'GET', '/notices/%d' % HIDDEN_NOTICE_ID, expected_code=NOTICE_NOT_FOUND)

        # 정리
        if order_id:
            self.call('주문 취소', 'PUT', '/orders/%s/cancel' % order_id, {'reason': 'test'}, token=access_token)
        self.call('찜 해제', 'DELETE', '/favorites/1', token=access_token)
        self.call('장바구니 비우기', 'DELETE', '/cart', token=access_token)

        # 토큰 오류 응답
        self.call('위조 토큰 거부', 'GET', '/user/me', token='abc.def.ghi', expected_code=TOKEN_WRONG)
        self.call('잘못된 refresh 토큰 거부', 'POST', '/auth/refresh',
                  {'accessToken': access_token, 'refreshToken': 'abc.def.ghi'}, expected_code=TOKEN_REFRESH_EXPIRE)

        # 토큰 갱신 → 로그아웃 → 로그아웃한 토큰 거부
        new_tokens = self.call('토큰 갱신', 'POST', '/auth/refresh',
                               {'accessToken': access_token, 'refreshToken': refresh_token}, token=access_token)
        latest_token = new_tokens.get('accessToken', access_token)
        time.sleep(1.1)  # 토큰 무효화는 초 단위 → 발급과 다른 초에 로그아웃
        self.call('로그아웃', 'POST', '/auth/logout', token=latest_token)
        self.call('로그아웃 토큰 거부', 'GET', '/user/me', token=latest_token, expected_code=TOKEN_BLACKLISTED)
        self.call('토큰 없이 인증 API', 'GET', '/cart', expected_code=UNAUTHORIZED)

        # 공개 경로 축소 확인: 조회만 공개, 쓰기·내 정보는 로그인 필요
        self.call('토큰 없이 내 정보 거부', 'GET', '/user/me', expected_code=UNAUTHORIZED)
        self.call('토큰 없이 내 리뷰 거부', 'GET', '/reviews/my', expected_code=UNAUTHORIZED)
        self.call('토큰 없이 리뷰 작성 거부', 'POST', '/reviews',
                  {'storeId': 1, 'orderOrderId': 'X', 'rating': 5, 'content': 'x'}, expected_code=UNAUTHORIZED)

        self.run_account_settings(user_id + 's', password)

    def run_account_settings(self, user_id, password):
        """환경설정: 내 정보 수정 → 비밀번호 변경(모든 토큰 무효화) → 회원탈퇴(같은 아이디 재가입 불가)"""
        self.call('[설정] 회원가입', 'POST', '/user/register', {'id': user_id, 'pw': password, 'name': 'before'})
        token = self.call('[설정] 로그인', 'POST', '/login', {'username': user_id, 'password': password}).get('accessToken')

        self.call('[설정] 내 정보 수정', 'PUT', '/user/me', {'name': '바뀐이름', 'tel': '01033334444'}, token=token,
                  check=lambda data: None if data.get('name') == '바뀐이름' else 'name=%s' % data.get('name'))
        self.call('[설정] 수정한 내 정보가 바로 보임', 'GET', '/user/me', token=token,
                  check=lambda data: None if data.get('tel') == '01033334444' else 'tel=%s' % data.get('tel'))
        self.call('[설정] 전화번호 형식 거부', 'PUT', '/user/me', {'name': 'x', 'tel': '010-3333-4444'}, token=token,
                  expected_code=INVALID_INPUT)
        self.call('[설정] 이름 빈 값 거부', 'PUT', '/user/me', {'name': ' ', 'tel': ''}, token=token,
                  expected_code=INVALID_INPUT)

        new_password = 'Changed123!'
        self.call('[설정] 현재 비밀번호 틀리면 변경 거부', 'PUT', '/user/me/password',
                  {'currentPassword': 'wrong', 'newPassword': new_password}, token=token, expected_code=INVALID_PASSWORD)
        self.call('[설정] 새 비밀번호 8자 미만 거부', 'PUT', '/user/me/password',
                  {'currentPassword': password, 'newPassword': 'short'}, token=token, expected_code=INVALID_INPUT)
        # 토큰 무효화는 초 단위라, 같은 초에 발급된 토큰은 막히지 않는다 → 발급과 다른 초가 되도록 잠시 기다림
        time.sleep(1.1)
        self.call('[설정] 비밀번호 변경', 'PUT', '/user/me/password',
                  {'currentPassword': password, 'newPassword': new_password}, token=token)
        self.call('[설정] 변경 전 토큰 거부', 'GET', '/user/me', token=token, expected_code=TOKEN_BLACKLISTED)
        self.call('[설정] 옛 비밀번호 로그인 거부', 'POST', '/login', {'username': user_id, 'password': password},
                  expected_code=INVALID_PASSWORD)
        token = self.call('[설정] 새 비밀번호 로그인', 'POST', '/login',
                          {'username': user_id, 'password': new_password}).get('accessToken')

        self.call('[설정] 비밀번호 틀리면 탈퇴 거부', 'DELETE', '/user/me', {'password': 'wrong'}, token=token,
                  expected_code=INVALID_PASSWORD)
        time.sleep(1.1)
        self.call('[설정] 회원탈퇴', 'DELETE', '/user/me', {'password': new_password}, token=token)
        self.call('[설정] 탈퇴 후 토큰 거부', 'GET', '/user/me', token=token, expected_code=TOKEN_BLACKLISTED)
        self.call('[설정] 탈퇴 후 로그인 거부', 'POST', '/login', {'username': user_id, 'password': new_password},
                  expected_code=USER_NOT_FOUND)
        self.call('[설정] 탈퇴한 아이디 재가입 거부', 'POST', '/user/register',
                  {'id': user_id, 'pw': password, 'name': 'again'}, expected_code=DUPLICATED)

    def report(self):
        for label, is_passed, code, description in self.results:
            print('%s  %s  %s%s' % ('OK  ' if is_passed else 'FAIL', label, code,
                                    '' if is_passed else '  ' + str(description)[:100]))
        passed_count = sum(1 for result in self.results if result[1])
        print('\n결과: %d / %d 통과' % (passed_count, len(self.results)))
        return passed_count == len(self.results)


def mark_order_delivered(order_id, container='winter-mariadb'):
    """사장님 API 가 생기기 전까지 주문 완료(DELIVERED) 처리는 로컬 DB 컨테이너에 직접 한다. 실패하면 False"""
    sql = "UPDATE tb_order SET status = 'DELIVERED' WHERE order_id = '%s'" % order_id
    command = ['docker', 'exec', container, 'sh', '-c',
               'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" winter_app -e "%s"' % sql]
    try:
        return subprocess.run(command, capture_output=True, timeout=20).returncode == 0
    except (OSError, subprocess.SubprocessError):
        return False


def check_amount(data, expected_amount):
    amount = data.get('totalAmount')
    if amount is None or float(amount) != expected_amount:
        return 'totalAmount=%s (기대 %s)' % (amount, expected_amount)
    return None


def check_store_list(data):
    """매장 목록: 같은 매장이 중복되지 않고, 이미지가 있는 매장은 imageUrl 이 채워져야 한다"""
    stores = data.get('stores', [])
    store_ids = [store.get('id') for store in stores]
    if len(store_ids) != len(set(store_ids)):
        return '매장 중복: %s' % store_ids
    if any(not store.get('imageUrl') for store in stores):
        return 'imageUrl 누락'
    return None


def check_next_page(first_page, data):
    stores = data.get('stores', [])
    first_ids = [store.get('id') for store in first_page.get('stores', [])]
    if len(stores) != 1:
        return '1건이 아님'
    if stores[0].get('id') in first_ids:
        return '1페이지와 같은 매장'
    return None


def check_store_detail_image(data):
    image_url = (data.get('store') or data).get('imageUrl')
    if not image_url:
        return 'imageUrl 누락'
    if 'second' in image_url:
        return '대표 이미지가 아님: %s' % image_url
    return None


def main():
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    parser = argparse.ArgumentParser(description='윈터푸디스 API 스모크 테스트')
    parser.add_argument('--base-url', default='http://localhost:8085')
    parser.add_argument('--user-id', default='smoke_%d' % int(time.time()), help='테스트용 가입 아이디(기본: 매번 새로 생성)')
    args = parser.parse_args()

    smoke_test = SmokeTest(args.base_url)
    smoke_test.run(args.user_id)
    sys.exit(0 if smoke_test.report() else 1)


if __name__ == '__main__':
    main()
