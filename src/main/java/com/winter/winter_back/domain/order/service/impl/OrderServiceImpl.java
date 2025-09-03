package com.winter.winter_back.domain.order.service.impl;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.order.dao.OrderDao;
import com.winter.winter_back.domain.order.dao.OrderHistoryDao;
import com.winter.winter_back.domain.order.dao.OrderItemDao;
import com.winter.winter_back.domain.order.dto.*;
import com.winter.winter_back.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * description    :
 * fileName       : OrderServiceImpl
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final OrderHistoryDao orderHistoryDao;

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest, String userId) {
        try {
            // 주문 ID 생성
            String orderId = generateOrderId();

            // 주문 정보 생성
            OrderDto orderDto = OrderDto.builder()
                    .orderId(orderId)
                    .storeId(orderRequest.getStoreId())
                    .userId(userId)
                    .totalAmount(orderRequest.getTotalAmount())
                    .deliveryFee(orderRequest.getDeliveryFee())
                    .orderRequest(orderRequest.getOrderRequest())
                    .status("PENDING")
                    .createdDatetime(LocalDateTime.now())
                    .build();

            // 배송 정보 설정
            DeliveryInfoDto deliveryInfo = DeliveryInfoDto.builder()
                    .address(orderRequest.getDeliveryAddress().getAddress())
                    .detailAddress(orderRequest.getDeliveryAddress().getDetailAddress())
                    .phone(orderRequest.getDeliveryAddress().getPhone())
                    .build();
            orderDto.setDeliveryInfo(deliveryInfo);

            // 주문 저장
            orderDao.insertOrder(orderDto);

            // 주문 항목 저장
            for (OrderRequest.OrderItem item : orderRequest.getItems()) {
                OrderItemDto orderItemDto = OrderItemDto.builder()
                        .orderId(orderId)
                        .menuId(item.getMenuId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .specialRequest(item.getSpecialRequest())
                        .build();

                orderItemDao.insertOrderItem(orderItemDto);
            }

            // 주문 히스토리 생성
            createOrderHistory(orderId, "ORDER_PLACED", "주문이 접수되었습니다.", userId);

            return OrderResponse.builder()
                    .orderId(orderId)
                    .status("PENDING")
                    .message("주문이 성공적으로 접수되었습니다.")
                    .estimatedTime("30분 - 40분")
                    .build();

        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BizException("주문 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    public OrderConfirmResponse confirmOrder(OrderConfirmRequest confirmRequest, String userId) {
        try {
            // 주문 존재 여부 확인
            OrderDto order = orderDao.selectOrderById(confirmRequest.getOrderId());
            if (order == null) {
                throw new BizException("존재하지 않는 주문입니다.");
            }

            if (!order.getUserId().equals(userId)) {
                throw new BizException("주문에 대한 권한이 없습니다.");
            }

            // 주문 상태 업데이트
            orderDao.updateOrderStatus(confirmRequest.getOrderId(), "CONFIRMED");

            // 주문 히스토리 생성
            createOrderHistory(confirmRequest.getOrderId(), "ORDER_CONFIRMED",
                    "주문이 확인되었습니다. 예상 시간: " + confirmRequest.getEstimatedTime(), userId);

            return OrderConfirmResponse.builder()
                    .orderId(confirmRequest.getOrderId())
                    .status("CONFIRMED")
                    .estimatedTime(confirmRequest.getEstimatedTime())
                    .message("주문이 확인되었습니다.")
                    .build();

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("주문 확인 중 오류 발생: {}", e.getMessage(), e);
            throw new BizException("주문 확인 처리 중 오류가 발생했습니다.");
        }
    }


    @Override
    @Transactional(readOnly = true)
    public ListResponseDto<OrderListResponse> getOrderList(String userId, int page, int limit, String status) {
        try {
            int offset = (page - 1) * limit;

            List<OrderListResponse> orders = orderDao.selectOrderList(userId, status, offset, limit);
            int totalCount = orderDao.selectOrderCount(userId, status);

            ListResponseDto<OrderListResponse> response = new ListResponseDto<>();
            response.setTotalCount(totalCount);
            response.setDataList(orders);

            return response;

        } catch (Exception e) {
            log.error("주문 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BizException("주문 목록 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String orderId, String userId) {
        try {
            OrderDetailResponse orderDetail = orderDao.selectOrderDetail(orderId);
            if (orderDetail == null) {
                throw new BizException("존재하지 않는 주문입니다.");
            }

            if (!orderDetail.getUserId().equals(userId)) {
                throw new BizException("주문에 대한 권한이 없습니다.");
            }

            // 주문 항목 조회
            List<OrderDetailResponse.OrderItem> items = orderItemDao.selectOrderItems(orderId);
            orderDetail.setItems(items);

            return orderDetail;

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("주문 상세 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BizException("주문 상세 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingResponse getOrderTracking(String orderId, String userId) {
        try {
            OrderDto order = orderDao.selectOrderById(orderId);
            if (order == null) {
                throw new BizException("존재하지 않는 주문입니다.");
            }

            if (!order.getUserId().equals(userId)) {
                throw new BizException("주문에 대한 권한이 없습니다.");
            }

            // 주문 히스토리 조회
            List<OrderHistoryDto> histories = orderHistoryDao.selectOrderHistories(orderId);

            return OrderTrackingResponse.builder()
                    .orderId(orderId)
                    .currentStatus(order.getStatus())
                    .histories(histories)
                    .estimatedTime(getEstimatedTimeByStatus(order.getStatus()))
                    .build();

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("주문 추적 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BizException("주문 추적 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public OrderResponse cancelOrder(String orderId, OrderCancelRequest cancelRequest, String userId) {
        try {
            OrderDto order = orderDao.selectOrderById(orderId);
            if (order == null) {
                throw new BizException("존재하지 않는 주문입니다.");
            }

            if (!order.getUserId().equals(userId)) {
                throw new BizException("주문에 대한 권한이 없습니다.");
            }

            // 취소 가능한 상태인지 확인
            if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
                throw new BizException("취소할 수 없는 주문 상태입니다.");
            }

            // 주문 상태 업데이트
            orderDao.updateOrderStatus(orderId, "CANCELLED");

            // 주문 히스토리 생성
            createOrderHistory(orderId, "ORDER_CANCELLED",
                    "주문이 취소되었습니다. 사유: " + cancelRequest.getReason(), userId);

            return OrderResponse.builder()
                    .orderId(orderId)
                    .status("CANCELLED")
                    .message("주문이 취소되었습니다.")
                    .build();

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("주문 취소 중 오류 발생: {}", e.getMessage(), e);
            throw new BizException("주문 취소 처리 중 오류가 발생했습니다.");
        }
    }

    private String generateOrderId() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void createOrderHistory(String orderId, String status, String description, String userId) {
        OrderHistoryDto history = OrderHistoryDto.builder()
                .orderId(orderId)
                .status(status)
                .description(description)
                .createdBy(userId)
                .createdDatetime(LocalDateTime.now())
                .build();

        orderHistoryDao.insertOrderHistory(history);
    }

    private String getEstimatedTimeByStatus(String status) {
        switch (status) {
            case "PENDING":
                return "주문 접수 대기중";
            case "CONFIRMED":
                return "30분 - 40분";
            case "PREPARING":
                return "20분 - 30분";
            case "DELIVERING":
                return "10분 - 15분";
            case "DELIVERED":
                return "배달 완료";
            case "CANCELLED":
                return "주문 취소됨";
            default:
                return "확인 중";
        }
    }
}