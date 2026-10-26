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
import sys
import time
import urllib.error
import urllib.parse
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
PERMISSION_DENIED = '40002001'
MENU_SOLD_OUT = '62001003'
INVALID_ORDER_STATUS_CHANGE = '63001013'
REJECT_REASON_REQUIRED = '63001014'
INVALID_STORE_STATUS = '61001008'
INVALID_BUSINESS_HOURS = '61001005'
ORDER_NOT_FOUND = '63001001'
OWNER_ID, OWNER_PASSWORD = 'owner1', 'Owner1234!'      # 테스트 데이터: 1번 매장 사장님
OTHER_OWNER_ID = 'owner2'                              # 2번 매장 사장님
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
        option_order = self.call('주문 생성(옵션 추가금)', 'POST', '/orders',
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
        # 사장님이 확인 → 조리 → 픽업대기 → 픽업완료로 처리
        owner_token = self.call('[사장님] 로그인', 'POST', '/login',
                                {'username': OWNER_ID, 'password': OWNER_PASSWORD}).get('accessToken')
        is_delivered = False
        if review_order_id and owner_token:
            for next_status in ('CONFIRMED', 'PREPARING', 'DELIVERING', 'DELIVERED'):
                changed = self.call('[사장님] 주문 단계 → %s' % next_status, 'PATCH',
                                    '/owner/orders/%s/status' % review_order_id, {'status': next_status},
                                    token=owner_token,
                                    check=lambda data, expected=next_status: None if data.get('status') == expected
                                    else 'status=%s' % data.get('status'))
                is_delivered = changed.get('status') == 'DELIVERED'
            self.call('[손님] 진행 이력에 사장님 처리 단계', 'GET', '/orders/%s/tracking' % review_order_id,
                      token=access_token,
                      check=lambda data: None if [h.get('status') for h in data.get('histories', [])][-1:] == ['ORDER_DELIVERED']
                      else '마지막 이력=%s' % [h.get('status') for h in data.get('histories', [])][-1:])
        if is_delivered:
            self.call('받아간 주문 리뷰 작성 가능', 'GET', '/reviews/order/%s/writable' % review_order_id,
                      token=access_token, check=lambda data: None if data is True else '작성 가능해야 함')
            self.call('리뷰 작성', 'POST', '/reviews',
                      {'storeId': 1, 'orderOrderId': review_order_id, 'rating': 5, 'content': 'good'},
                      token=access_token)
            self.call('같은 주문 리뷰 중복 거부', 'POST', '/reviews',
                      {'storeId': 1, 'orderOrderId': review_order_id, 'rating': 4, 'content': 'again'},
                      token=access_token, expected_code=REVIEW_ALREADY_EXISTS)
        self.call('가게 리뷰 목록(작성자 아이디 숨김)', 'GET', '/reviews/store/1',
                  check=lambda data: None if all(review.get('userId') is None for review in data.get('dataList', []))
                  else '공개 목록에 userId 노출')
        self.call('내 리뷰', 'GET', '/reviews/my', token=access_token)
        self.call('리뷰 통계', 'GET', '/reviews/store/1/stats')

        # 검색: 가게 이름·간식 종류 이름·메뉴 이름으로 찾고, % 는 와일드카드로 쓰이지 않는다
        self.call('검색: 가게 이름(호떡)', 'GET', '/stores/search?keyword=%s' % urllib.parse.quote('호떡'),
                  check=lambda data: None if 2 in [st.get('id') for st in data.get('stores', [])] else '호떡 코너 없음')
        self.call('검색: 메뉴 이름(슈크림)', 'GET', '/stores/search?keyword=%s' % urllib.parse.quote('슈크림'),
                  check=lambda data: None if 1 in [st.get('id') for st in data.get('stores', [])] else '붕어빵 하우스 없음')
        self.call('검색: 간식 종류(붕어빵)', 'GET', '/stores/search?keyword=%s' % urllib.parse.quote('붕어빵'),
                  check=lambda data: None if 1 in [st.get('id') for st in data.get('stores', [])] else '붕어빵 하우스 없음')
        self.call('검색: % 는 와일드카드 아님', 'GET', '/stores/search?keyword=%25',
                  check=lambda data: None if data.get('totalCount') == 0 else 'totalCount=%s' % data.get('totalCount'))
        self.call('검색: 없는 검색어', 'GET', '/stores/search?keyword=%s' % urllib.parse.quote('피자'),
                  check=lambda data: None if data.get('totalCount') == 0 else 'totalCount=%s' % data.get('totalCount'))

        # 공지사항 (비로그인 조회)
        self.call('공지 목록(고정 먼저·비공개 제외)', 'GET', '/notices',
                  check=lambda data: None if (data.get('dataList') and data['dataList'][0].get('isPinned') == 'Y'
                                              and all(notice.get('idx') != HIDDEN_NOTICE_ID for notice in data['dataList'])
                                              and all(notice.get('content') is None for notice in data['dataList']))
                  else '고정 공지가 맨 위가 아니거나 비공개·본문 포함')
        self.call('공지 상세', 'GET', '/notices/1', check=lambda data: None if data.get('content') else '본문 없음')
        self.call('비공개 공지 상세 거부', 'GET', '/notices/%d' % HIDDEN_NOTICE_ID, expected_code=NOTICE_NOT_FOUND)

        if owner_token:
            self.run_owner(owner_token, access_token, pickup_body)

        # 정리 (접수 상태로 남는 주문이 사장님 새 주문에 쌓이지 않게 모두 취소)
        if option_order.get('orderId'):
            self.call('옵션 주문 취소', 'PUT', '/orders/%s/cancel' % option_order['orderId'], {'reason': 'test'},
                      token=access_token)
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

    def run_owner(self, owner_token, customer_token, pickup_body):
        """사장님: 권한·내 가게·대기시간·영업 상태·주문 거절·순서 위반·다른 가게 주문·품절"""
        self.call('[사장님] 내 정보에 사장님·가게 표시', 'GET', '/user/me', token=owner_token,
                  check=lambda data: None if data.get('isOwner') is True and data.get('storeId') == 1
                  else 'isOwner=%s storeId=%s' % (data.get('isOwner'), data.get('storeId')))
        self.call('[손님] 내 정보는 사장님 아님', 'GET', '/user/me', token=customer_token,
                  check=lambda data: None if data.get('isOwner') is False else 'isOwner=%s' % data.get('isOwner'))
        self.call('[손님] 사장님 API 거부', 'GET', '/owner/store', token=customer_token, expected_code=PERMISSION_DENIED)
        self.call('[사장님] 내 가게·오늘 현황', 'GET', '/owner/store', token=owner_token,
                  check=lambda data: None if data.get('storeId') == 1 and data.get('todayOrderCount') is not None
                  else '가게 정보 없음')

        # 대기시간: 손님 가게 상세의 조리 시간으로 보인다
        self.call('[사장님] 대기시간 25분', 'PATCH', '/owner/store/wait-time', {'minutes': 25}, token=owner_token)
        self.call('[손님] 가게 상세에 대기시간 반영', 'GET', '/stores/1',
                  check=lambda data: None if data.get('store', {}).get('deliveryTime') == 25
                  else 'deliveryTime=%s' % data.get('store', {}).get('deliveryTime'))
        self.call('[사장님] 대기시간 범위 초과 거부', 'PATCH', '/owner/store/wait-time', {'minutes': 500},
                  token=owner_token, expected_code=INVALID_INPUT)
        self.call('[사장님] 대기시간 원래대로', 'PATCH', '/owner/store/wait-time', {'minutes': 20}, token=owner_token)

        # 영업 상태: 잠시 쉼이면 손님 주문 거부
        self.call('[사장님] 잠시 쉼', 'PATCH', '/owner/store/status', {'status': 'TEMPORARILY_CLOSED'}, token=owner_token)
        self.call('[손님] 쉬는 가게 주문 거부', 'POST', '/orders', pickup_body, token=customer_token,
                  expected_code=RESTAURANT_CLOSED)
        self.call('[사장님] 잘못된 영업 상태 거부', 'PATCH', '/owner/store/status', {'status': 'SLEEPING'},
                  token=owner_token, expected_code=INVALID_STORE_STATUS)
        self.call('[사장님] 영업 재개', 'PATCH', '/owner/store/status', {'status': 'OPEN'}, token=owner_token)

        # 주문 거절·순서 위반·다른 가게
        order = self.call('[손님] 거절용 주문', 'POST', '/orders', pickup_body, token=customer_token)
        order_id = order.get('orderId')
        if order_id:
            self.call('[사장님] 단계 건너뛰기 거부(접수→픽업완료)', 'PATCH', '/owner/orders/%s/status' % order_id,
                      {'status': 'DELIVERED'}, token=owner_token, expected_code=INVALID_ORDER_STATUS_CHANGE)
            self.call('[사장님] 사유 없는 거절 거부', 'PATCH', '/owner/orders/%s/status' % order_id,
                      {'status': 'CANCELLED'}, token=owner_token, expected_code=REJECT_REASON_REQUIRED)
            other_token = self.call('[다른 사장님] 로그인', 'POST', '/login',
                                    {'username': OTHER_OWNER_ID, 'password': OWNER_PASSWORD}).get('accessToken')
            self.call('[다른 사장님] 남의 가게 주문 처리 거부', 'PATCH', '/owner/orders/%s/status' % order_id,
                      {'status': 'CONFIRMED'}, token=other_token, expected_code=ORDER_NOT_FOUND)
            self.call('[사장님] 새 주문 목록에 보임', 'GET', '/owner/orders?status=PENDING', token=owner_token,
                      check=lambda data: None if any(o.get('orderId') == order_id for o in data.get('dataList', []))
                      else '새 주문 목록에 없음')
            self.call('[사장님] 주문 거절', 'PATCH', '/owner/orders/%s/status' % order_id,
                      {'status': 'CANCELLED', 'reason': '재료 소진'}, token=owner_token,
                      check=lambda data: None if data.get('status') == 'CANCELLED' else 'status=%s' % data.get('status'))
            self.call('[사장님] 거절한 주문 다시 처리 거부', 'PATCH', '/owner/orders/%s/status' % order_id,
                      {'status': 'CONFIRMED'}, token=owner_token, expected_code=INVALID_ORDER_STATUS_CHANGE)

        # 품절: 손님은 품절 메뉴를 주문할 수 없다
        self.call('[사장님] 메뉴 목록', 'GET', '/owner/menus', token=owner_token,
                  check=lambda data: None if isinstance(data, list) and data else '메뉴 없음')
        self.call('[사장님] 팥 붕어빵 품절', 'PATCH', '/owner/menus/1/sold-out', {'isSoldOut': True}, token=owner_token)
        self.call('[손님] 품절 메뉴 주문 거부', 'POST', '/orders', pickup_body, token=customer_token,
                  expected_code=MENU_SOLD_OUT)
        self.call('[사장님] 품절 해제', 'PATCH', '/owner/menus/1/sold-out', {'isSoldOut': False}, token=owner_token)
        self.call('[사장님] 남의 가게 메뉴 품절 거부', 'PATCH', '/owner/menus/3/sold-out', {'isSoldOut': True},
                  token=owner_token, expected_code=MENU_NOT_FOUND)

        # 메뉴 추가 → 손님 메뉴에 보임 → 수정 → 삭제하면 손님 메뉴에서 사라짐
        def public_menu_names():
            menu = self.call('[손님] 가게 메뉴', 'GET', '/stores/1/menu')
            return [m.get('name') for c in menu.get('categories', []) for m in c.get('menus', [])]

        added = self.call('[사장님] 메뉴 추가(새 카테고리)', 'POST', '/owner/menus',
                          {'name': '스모크 붕어빵', 'description': '테스트', 'price': 4000, 'categoryName': '스모크'},
                          token=owner_token,
                          check=lambda data: None if data.get('menuId') and data.get('categoryName') == '스모크'
                          else '추가 결과=%s' % data)
        menu_id = added.get('menuId')
        if menu_id:
            self.call('[손님] 추가한 메뉴가 보임', 'GET', '/stores/1/menu',
                      check=lambda data: None if '스모크 붕어빵' in public_menu_names() else '추가한 메뉴 없음')
            self.call('[사장님] 메뉴 수정', 'PUT', '/owner/menus/%s' % menu_id,
                      {'name': '스모크 붕어빵2', 'price': 4500, 'categoryName': '기본'}, token=owner_token,
                      check=lambda data: None if data.get('price') == 4500 and data.get('categoryName') == '기본'
                      else '수정 결과=%s' % data)
            self.call('[사장님] 가격 음수 거부', 'PUT', '/owner/menus/%s' % menu_id,
                      {'name': 'x', 'price': -1}, token=owner_token, expected_code=INVALID_INPUT)
            other_token = self.call('[다른 사장님] 로그인(메뉴)', 'POST', '/login',
                                    {'username': OTHER_OWNER_ID, 'password': OWNER_PASSWORD}).get('accessToken')
            self.call('[다른 사장님] 남의 메뉴 수정 거부', 'PUT', '/owner/menus/%s' % menu_id,
                      {'name': 'x', 'price': 1}, token=other_token, expected_code=MENU_NOT_FOUND)
            self.call('[사장님] 메뉴 삭제', 'DELETE', '/owner/menus/%s' % menu_id, token=owner_token)
            self.call('[손님] 삭제한 메뉴는 안 보임', 'GET', '/stores/1/menu',
                      check=lambda data: None if not any(name.startswith('스모크') for name in public_menu_names())
                      else '삭제한 메뉴가 보임')
            self.call('[손님] 메뉴 없는 카테고리는 안 보임', 'GET', '/stores/1/menu',
                      check=lambda data: None if all(c.get('menus') for c in data.get('categories', []))
                      else '빈 카테고리 노출')
            self.call('[사장님] 삭제한 메뉴 다시 삭제 거부', 'DELETE', '/owner/menus/%s' % menu_id, token=owner_token,
                      expected_code=MENU_NOT_FOUND)

        # 가게 정보 수정 → 손님 가게 상세에 반영 → 원복
        store = self.call('[사장님] 가게 정보', 'GET', '/owner/store', token=owner_token)
        self.call('[사장님] 가게 정보 수정', 'PUT', '/owner/store',
                  {'name': '붕어빵 하우스(수정)', 'description': '소개 수정', 'phone': '0299998888', 'minOrderAmount': 4000},
                  token=owner_token,
                  check=lambda data: None if data.get('minOrderAmount') == 4000 else '수정 결과=%s' % data)
        self.call('[손님] 가게 상세에 반영', 'GET', '/stores/1',
                  check=lambda data: None if data.get('store', {}).get('name') == '붕어빵 하우스(수정)'
                  else 'name=%s' % data.get('store', {}).get('name'))
        self.call('[사장님] 전화번호 형식 거부', 'PUT', '/owner/store',
                  {'name': 'x', 'phone': '02-1234', 'minOrderAmount': 0}, token=owner_token, expected_code=INVALID_INPUT)
        self.call('[사장님] 가게 정보 원래대로', 'PUT', '/owner/store',
                  {'name': store.get('name'), 'description': store.get('description'), 'phone': store.get('phone'),
                   'minOrderAmount': store.get('minOrderAmount')}, token=owner_token)

        # 영업시간: 월~일 7개, 휴무가 아니면 시각 필수·같은 시각 불가·요일 중복 불가
        hours = self.call('[사장님] 영업시간 7일', 'GET', '/owner/store/hours', token=owner_token,
                          check=lambda data: None if isinstance(data, list) and len(data) == 7 else '7일이 아님')
        week = [{'dayOfWeek': d, 'openTime': '10:00', 'closeTime': '22:00', 'isHoliday': d == 'SUNDAY'}
                for d in ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')]
        self.call('[사장님] 영업시간 저장(일요일 휴무)', 'PUT', '/owner/store/hours', {'days': week}, token=owner_token,
                  check=lambda data: None if [d.get('isHoliday') for d in data] == [False] * 6 + [True]
                  and data[6].get('openTime') is None else '저장 결과=%s' % data)
        self.call('[사장님] 휴무 아닌데 시각 없음 거부', 'PUT', '/owner/store/hours',
                  {'days': [{'dayOfWeek': 'MONDAY', 'isHoliday': False}]}, token=owner_token,
                  expected_code=INVALID_BUSINESS_HOURS)
        self.call('[사장님] 요일 중복 거부', 'PUT', '/owner/store/hours',
                  {'days': [week[0], week[0]]}, token=owner_token, expected_code=INVALID_BUSINESS_HOURS)
        self.call('[사장님] 시각 형식 거부', 'PUT', '/owner/store/hours',
                  {'days': [dict(week[0], openTime='25:00')]}, token=owner_token, expected_code=INVALID_INPUT)
        if isinstance(hours, list) and hours:
            self.call('[사장님] 영업시간 원래대로', 'PUT', '/owner/store/hours',
                      {'days': [h for h in hours if h.get('openTime') or h.get('isHoliday')]
                       or [dict(week[0])]}, token=owner_token)

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
