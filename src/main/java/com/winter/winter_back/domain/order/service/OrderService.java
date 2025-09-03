package com.winter.winter_back.domain.order.service;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.domain.order.dto.*;

/**
 * description    :
 * fileName       : OrderService
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */
public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest, String userId);

    OrderConfirmResponse confirmOrder(OrderConfirmRequest confirmRequest, String userId);

    ListResponseDto<OrderListResponse> getOrderList(String userId, int page, int limit, String status);

    OrderDetailResponse getOrderDetail(String orderId, String userId);

    OrderTrackingResponse getOrderTracking(String orderId, String userId);

    OrderResponse cancelOrder(String orderId, OrderCancelRequest cancelRequest, String userId);
}