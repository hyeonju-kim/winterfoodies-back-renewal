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
import urllib.request

SUCCESS = '20000000'
UNAUTHORIZED = '40001001'
TOKEN_WRONG = '40001007'
TOKEN_BLACKLISTED = '40001008'
TOKEN_REFRESH_EXPIRE = '40001009'
NOT_SUPPORTED_CODE = '30003003'
REVIEW_ALREADY_EXISTS = '64001002'


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
                text = response.read().decode('utf-8')
        except urllib.error.HTTPError as error:
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

        data = payload.get('data') or {}
        is_passed = code == expected_code
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
        self.call('홈 피드', 'GET', '/home/feed?lat=37.5&lng=127.0', token=access_token)

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
        order = self.call('주문 생성', 'POST', '/orders',
                          {'storeId': 1, 'items': [{'menuId': 1, 'quantity': 2, 'price': 3000}],
                           'deliveryAddress': {'address': '서울 강남구 1', 'detailAddress': '101',
                                               'phone': '01011112222'},
                           'paymentMethod': 'CARD', 'totalAmount': 7000, 'deliveryFee': 1000}, token=access_token)
        order_id = order.get('orderId')
        self.call('주문 목록', 'GET', '/orders', token=access_token)
        if order_id:
            self.call('주문 상세', 'GET', '/orders/%s' % order_id, token=access_token)
            self.call('주문 추적', 'GET', '/orders/%s/tracking' % order_id, token=access_token)

        # 리뷰
        if order_id:
            self.call('리뷰 작성 가능 여부', 'GET', '/reviews/order/%s/writable' % order_id, token=access_token,
                      check=lambda data: None if data is True else '작성 가능해야 함')
            self.call('리뷰 작성', 'POST', '/reviews',
                      {'storeId': 1, 'orderOrderId': order_id, 'rating': 5, 'content': 'good'}, token=access_token)
            self.call('같은 주문 리뷰 중복 거부', 'POST', '/reviews',
                      {'storeId': 1, 'orderOrderId': order_id, 'rating': 4, 'content': 'again'}, token=access_token,
                      expected_code=REVIEW_ALREADY_EXISTS)
        self.call('가게 리뷰 목록', 'GET', '/reviews/store/1')
        self.call('내 리뷰', 'GET', '/reviews/my', token=access_token)
        self.call('리뷰 통계', 'GET', '/reviews/store/1/stats')

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
        self.call('로그아웃', 'POST', '/auth/logout', token=latest_token)
        self.call('로그아웃 토큰 거부', 'GET', '/user/me', token=latest_token, expected_code=TOKEN_BLACKLISTED)
        self.call('토큰 없이 인증 API', 'GET', '/cart', expected_code=UNAUTHORIZED)

    def report(self):
        for label, is_passed, code, description in self.results:
            print('%s  %s  %s%s' % ('OK  ' if is_passed else 'FAIL', label, code,
                                    '' if is_passed else '  ' + str(description)[:100]))
        passed_count = sum(1 for result in self.results if result[1])
        print('\n결과: %d / %d 통과' % (passed_count, len(self.results)))
        return passed_count == len(self.results)


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
