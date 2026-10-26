package com.winter.winter_back.domain.owner.dao;

import com.winter.winter_back.domain.owner.dto.OwnerMenuResponse;
import com.winter.winter_back.domain.owner.dto.OwnerOrderResponse;
import com.winter.winter_back.domain.owner.dto.OwnerStoreResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
 */
@Mapper
public interface OwnerDao {

    Long getOwnerStoreId(@Param("userId") String userId); // 사장님 아이디로 내 가게 ID

    OwnerStoreResponse getOwnerStore(@Param("storeId") Long storeId); // 내 가게 정보 + 오늘 현황

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
}
