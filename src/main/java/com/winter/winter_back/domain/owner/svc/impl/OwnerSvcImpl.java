package com.winter.winter_back.domain.owner.svc.impl;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.order.dao.OrderHistoryDao;
import com.winter.winter_back.domain.order.dto.OrderHistoryDto;
import com.winter.winter_back.domain.order.dto.OrderStatus;
import com.winter.winter_back.domain.owner.dao.OwnerDao;
import com.winter.winter_back.domain.owner.dto.OwnerMenuResponse;
import com.winter.winter_back.domain.owner.dto.OwnerOrderResponse;
import com.winter.winter_back.domain.owner.dto.OwnerRequests;
import com.winter.winter_back.domain.owner.dto.OwnerStoreResponse;
import com.winter.winter_back.domain.owner.svc.OwnerSvc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * description    : 사장님 서비스 구현 - 영업 상태·대기시간, 주문 단계 처리, 메뉴 품절
 * fileName       : OwnerSvcImpl
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OwnerSvcImpl implements OwnerSvc {

    private static final Set<String> STORE_STATUSES = Set.of("OPEN", "TEMPORARILY_CLOSED", "CLOSED");
    private static final int MAX_LIMIT = 50;

    // 주문 단계는 이 순서로만 바뀐다: 접수 → 확인 → 조리 → 픽업대기 → 픽업완료, 확인 단계까지는 거절(취소) 가능
    private static final Map<OrderStatus, Set<OrderStatus>> NEXT_STATUSES = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.DELIVERING),
            OrderStatus.DELIVERING, Set.of(OrderStatus.DELIVERED));

    // 손님 화면 진행 상황에 쌓이는 이력 문구 (포장 주문 기준)
    private static final Map<OrderStatus, String> HISTORY_DESCRIPTIONS = Map.of(
            OrderStatus.CONFIRMED, "가게에서 주문을 확인했습니다.",
            OrderStatus.PREPARING, "조리를 시작했습니다.",
            OrderStatus.DELIVERING, "준비가 끝났어요. 가게에서 받아가세요.",
            OrderStatus.DELIVERED, "픽업이 완료되었습니다.",
            OrderStatus.CANCELLED, "가게에서 주문을 거절했습니다.");

    private final OwnerDao ownerDao;
    private final OrderHistoryDao orderHistoryDao;

    @Override
    @Transactional(readOnly = true)
    public OwnerStoreResponse getMyStore(String userId) {
        return ownerDao.getOwnerStore(getStoreId(userId));
    }

    /**
     * Author      : hj kim
     * Description : 영업 상태 변경. 영업중이 아니면 손님 주문이 거부된다(주문 서비스의 restaurant_closed)
     * Date        : 26. 10. 26.
     */
    @Override
    public OwnerStoreResponse modStoreStatus(String userId, OwnerRequests.StoreStatusRequest request) {
        if (!STORE_STATUSES.contains(request.getStatus())) {
            throw new BizException("invalid_store_status");
        }
        Long storeId = getStoreId(userId);
        ownerDao.modStoreStatus(storeId, request.getStatus());
        return ownerDao.getOwnerStore(storeId);
    }

    /**
     * Author      : hj kim
     * Description : 대기시간 변경. 손님 화면의 "조리 약 N분"(tb_store.delivery_time)으로 바로 보인다
     * Date        : 26. 10. 26.
     */
    @Override
    public OwnerStoreResponse modWaitTime(String userId, OwnerRequests.WaitTimeRequest request) {
        Long storeId = getStoreId(userId);
        ownerDao.modStoreWaitTime(storeId, request.getMinutes());
        return ownerDao.getOwnerStore(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public ListResponseDto<OwnerOrderResponse> getOrderList(String userId, List<String> statuses, int page, int limit) {
        Long storeId = getStoreId(userId);
        List<String> statusFilter = statuses == null || statuses.isEmpty() ? null : statuses;
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        int offset = (Math.max(page, 1) - 1) * safeLimit;

        ListResponseDto<OwnerOrderResponse> response = new ListResponseDto<>();
        response.setDataList(ownerDao.getOwnerOrderList(storeId, statusFilter, offset, safeLimit));
        response.setTotalCount(ownerDao.getOwnerOrderCount(storeId, statusFilter));
        return response;
    }

    /**
     * Author      : hj kim
     * Description : 주문 단계 변경. 정해진 다음 단계로만 바꿀 수 있고, 거절은 사유가 필요하다.
     *               손님이 같은 순간 취소하는 경우를 막기 위해 "지금 상태가 확인한 상태일 때만" 바꾸고(0건이면 거부),
     *               바뀌면 손님 진행 상황에 보일 이력(ORDER_상태)을 남긴다
     * Date        : 26. 10. 26.
     */
    @Override
    public OwnerOrderResponse modOrderStatus(String userId, String orderId, OwnerRequests.OrderStatusChangeRequest request) {
        Long storeId = getStoreId(userId);
        OwnerOrderResponse order = ownerDao.getOwnerOrder(storeId, orderId);
        if (order == null) {
            // 다른 가게 주문도 "없는 주문"으로 응답 (주문번호 존재 여부를 알려주지 않는다)
            throw new BizException("order_not_found");
        }

        OrderStatus current = OrderStatus.from(order.getStatus());
        OrderStatus next = OrderStatus.from(request.getStatus());
        if (current == null || next == null || !NEXT_STATUSES.getOrDefault(current, Set.of()).contains(next)) {
            throw new BizException("invalid_order_status_change");
        }
        String reason = request.getReason() == null ? "" : request.getReason().trim();
        if (next == OrderStatus.CANCELLED && reason.isEmpty()) {
            throw new BizException("reject_reason_required");
        }

        if (ownerDao.modOrderStatus(storeId, orderId, current.name(), next.name()) == 0) {
            throw new BizException("invalid_order_status_change");
        }

        String description = HISTORY_DESCRIPTIONS.get(next) + (reason.isEmpty() ? "" : " 사유: " + reason);
        orderHistoryDao.insertOrderHistory(OrderHistoryDto.builder()
                .orderId(orderId)
                .status("ORDER_" + next.name())
                .description(description)
                .createdBy(userId)
                .createdDatetime(LocalDateTime.now())
                .build());
        log.info("[modOrderStatus] 가게 {} 주문 {} : {} → {}", storeId, orderId, current, next);

        return ownerDao.getOwnerOrder(storeId, orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerMenuResponse> getMenuList(String userId) {
        return ownerDao.getOwnerMenuList(getStoreId(userId));
    }

    @Override
    public void modMenuSoldOut(String userId, Long menuId, OwnerRequests.SoldOutRequest request) {
        if (ownerDao.modMenuSoldOut(getStoreId(userId), menuId, request.getIsSoldOut()) == 0) {
            throw new BizException("menu_not_found");
        }
    }

    private Long getStoreId(String userId) {
        Long storeId = ownerDao.getOwnerStoreId(userId);
        if (storeId == null) {
            throw new BizException("owner_store_not_found");
        }
        return storeId;
    }
}
