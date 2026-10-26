package com.winter.winter_back.domain.owner.dao;

import com.winter.winter_back.domain.owner.dto.OwnerHoursResponse;
import com.winter.winter_back.domain.owner.dto.OwnerMenuResponse;
import com.winter.winter_back.domain.owner.dto.OwnerOrderResponse;
import com.winter.winter_back.domain.owner.dto.OwnerRequests;
import com.winter.winter_back.domain.owner.dto.OwnerSalesResponse;
import com.winter.winter_back.domain.owner.dto.OwnerSaveDtos;
import com.winter.winter_back.domain.owner.dto.OwnerStoreResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * description    : 사장님 DAO (모든 쿼리는 사장님 가게 ID 로 범위를 묶는다)
 * fileName       : OwnerDao
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 * 26. 10. 26.       hj kim             메뉴 추가·수정·삭제, 가게 정보, 영업시간 추가
 * 26. 10. 26.       hj kim             매출(날짜별 매출·인기 메뉴·취소 수) 추가
 * 26. 10. 26.       hj kim             가게 등록 추가
 */
@Mapper
public interface OwnerDao {

    Long getOwnerStoreId(@Param("userId") String userId); // 사장님 아이디로 내 가게 ID

    OwnerStoreResponse getOwnerStore(@Param("storeId") Long storeId); // 내 가게 정보 + 오늘 현황

    int regStore(OwnerSaveDtos.StoreSave store); // 가게 등록 (영업 종료 상태로 시작)

    int regStoreCategory(@Param("storeId") Long storeId,
                         @Param("categoryCode") String categoryCode,
                         @Param("categoryName") String categoryName); // 가게 간식 종류 (검색용 한글 이름)

    int modStoreStatus(@Param("storeId") Long storeId, @Param("status") String status); // 영업 상태 변경

    int modStoreWaitTime(@Param("storeId") Long storeId, @Param("minutes") int minutes); // 대기시간 변경

    List<OwnerOrderResponse> getOwnerOrderList(@Param("storeId") Long storeId,
                                               @Param("statuses") List<String> statuses,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit); // 내 가게 주문 목록 (최신순)

    int getOwnerOrderCount(@Param("storeId") Long storeId, @Param("statuses") List<String> statuses); // 내 가게 주문 수

    OwnerOrderResponse getOwnerOrder(@Param("storeId") Long storeId, @Param("orderId") String orderId); // 내 가게 주문 한 건

    int modOrderStatus(@Param("storeId") Long storeId,
                       @Param("orderId") String orderId,
                       @Param("fromStatus") String fromStatus,
                       @Param("toStatus") String toStatus); // 지금 상태가 fromStatus 일 때만 변경 (동시 변경 방지)

    List<OwnerMenuResponse> getOwnerMenuList(@Param("storeId") Long storeId); // 내 가게 메뉴 (카테고리·정렬 순)

    int modMenuSoldOut(@Param("storeId") Long storeId,
                       @Param("menuId") Long menuId,
                       @Param("isSoldOut") boolean isSoldOut); // 품절 켜고 끄기

    OwnerMenuResponse getOwnerMenu(@Param("storeId") Long storeId, @Param("menuId") Long menuId); // 내 가게 메뉴 한 건

    Long getMenuCategoryId(@Param("storeId") Long storeId, @Param("name") String name); // 카테고리 이름으로 ID

    int regMenuCategory(OwnerSaveDtos.MenuCategorySave category); // 메뉴 카테고리 추가 (맨 뒤 순서)

    int regMenu(OwnerSaveDtos.MenuSave menu); // 메뉴 추가 (카테고리 맨 뒤 순서)

    int modMenu(OwnerSaveDtos.MenuSave menu); // 메뉴 수정 (이름·설명·가격·카테고리)

    int delMenu(@Param("storeId") Long storeId, @Param("menuId") Long menuId); // 메뉴 삭제 (사용 안 함 - 지난 주문이 참조)

    int modStoreInfo(@Param("storeId") Long storeId, @Param("request") OwnerRequests.StoreInfoRequest request); // 가게 정보 수정

    List<OwnerHoursResponse> getStoreHours(@Param("storeId") Long storeId); // 요일별 영업시간

    int mergeStoreHours(@Param("storeId") Long storeId, @Param("day") OwnerRequests.DayHours day); // 요일 영업시간 저장 (있으면 수정)

    List<OwnerSalesResponse.DailySales> getDailySales(@Param("storeId") Long storeId,
                                                      @Param("fromDate") LocalDate fromDate,
                                                      @Param("toDate") LocalDate toDate); // 날짜별 매출 (받아간 주문)

    List<OwnerSalesResponse.TopMenu> getTopMenus(@Param("storeId") Long storeId,
                                                 @Param("fromDate") LocalDate fromDate,
                                                 @Param("toDate") LocalDate toDate,
                                                 @Param("limit") int limit); // 많이 팔린 메뉴

    int getCancelCount(@Param("storeId") Long storeId,
                       @Param("fromDate") LocalDate fromDate,
                       @Param("toDate") LocalDate toDate); // 취소 주문 수
}
