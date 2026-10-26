package com.winter.winter_back.domain.owner.svc;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.domain.owner.dto.OwnerHoursResponse;
import com.winter.winter_back.domain.owner.dto.OwnerMenuResponse;
import com.winter.winter_back.domain.owner.dto.OwnerOrderResponse;
import com.winter.winter_back.domain.owner.dto.OwnerRequests;
import com.winter.winter_back.domain.owner.dto.OwnerSalesResponse;
import com.winter.winter_back.domain.owner.dto.OwnerStoreResponse;

import java.util.List;

/**
 * description    : 사장님 서비스 인터페이스 (로그인한 사장님의 가게만 다룬다)
 * fileName       : OwnerSvc
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 * 26. 10. 26.       hj kim             메뉴 추가·수정·삭제, 가게 정보, 영업시간 추가
 * 26. 10. 26.       hj kim             매출 조회 추가
 */
public interface OwnerSvc {

    OwnerStoreResponse getMyStore(String userId); // 내 가게 + 오늘 현황

    OwnerStoreResponse modStoreStatus(String userId, OwnerRequests.StoreStatusRequest request); // 영업 상태 변경

    OwnerStoreResponse modWaitTime(String userId, OwnerRequests.WaitTimeRequest request); // 대기시간 변경

    ListResponseDto<OwnerOrderResponse> getOrderList(String userId, List<String> statuses, int page, int limit); // 주문 목록

    OwnerOrderResponse modOrderStatus(String userId, String orderId, OwnerRequests.OrderStatusChangeRequest request); // 주문 단계 변경

    List<OwnerMenuResponse> getMenuList(String userId); // 메뉴 목록

    void modMenuSoldOut(String userId, Long menuId, OwnerRequests.SoldOutRequest request); // 품절 켜고 끄기

    OwnerMenuResponse regMenu(String userId, OwnerRequests.MenuRequest request); // 메뉴 추가

    OwnerMenuResponse modMenu(String userId, Long menuId, OwnerRequests.MenuRequest request); // 메뉴 수정

    void delMenu(String userId, Long menuId); // 메뉴 삭제

    OwnerStoreResponse modStoreInfo(String userId, OwnerRequests.StoreInfoRequest request); // 가게 정보 수정

    List<OwnerHoursResponse> getHours(String userId); // 영업시간 (월~일 7개)

    List<OwnerHoursResponse> modHours(String userId, OwnerRequests.HoursRequest request); // 영업시간 수정

    OwnerSalesResponse getSales(String userId, String period); // 매출 (DAY 7일 / WEEK 8주 / MONTH 6개월)
}
