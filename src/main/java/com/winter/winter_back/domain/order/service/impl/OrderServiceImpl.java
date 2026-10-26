package com.winter.winter_back.domain.order.service.impl;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.order.dao.OrderDao;
import com.winter.winter_back.domain.order.dao.OrderHistoryDao;
import com.winter.winter_back.domain.order.dao.OrderItemDao;
import com.winter.winter_back.domain.order.dto.*;
import com.winter.winter_back.domain.order.service.OrderService;
import com.winter.winter_back.domain.menu.dao.MenuDao;
import com.winter.winter_back.domain.menu.dao.MenuOptionDao;
import com.winter.winter_back.domain.menu.dto.MenuDto;
import com.winter.winter_back.domain.menu.dto.MenuOptionDto;
import com.winter.winter_back.domain.store.dao.StoreDao;
import com.winter.winter_back.domain.store.dto.StoreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final StoreDao storeDao;
    private final MenuDao menuDao;
    private final MenuOptionDao menuOptionDao;

    /**
     * Author      : hj kim
     * Description : 주문 생성. 금액은 클라이언트 값을 쓰지 않고 메뉴·옵션·매장 배달비로 서버에서 다시 계산한다
     * Date        : 26. 10. 26.
     */
    @Override
    public OrderResponse createOrder(OrderRequest orderRequest, String userId) {
        StoreDto store = storeDao.findById(orderRequest.getStoreId());
        if (store == null) {
            throw new BizException("restaurant_not_found");
        }

        String orderId = generateOrderId();

        // 주문 항목별 단가(메뉴 가격 + 옵션 추가금) 계산
        List<OrderItemDto> orderItems = new ArrayList<>();
        BigDecimal itemsAmount = BigDecimal.ZERO;
        for (OrderRequest.OrderItem item : orderRequest.getItems()) {
            BigDecimal unitPrice = getUnitPrice(store.getId(), item);
            itemsAmount = itemsAmount.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

            orderItems.add(OrderItemDto.builder()
                    .orderId(orderId)
                    .menuId(item.getMenuId())
                    .quantity(item.getQuantity())
                    .price(unitPrice)
                    .specialRequest(item.getSpecialRequest())
                    .build());
        }

        if (store.getMinOrderAmount() != null && itemsAmount.compareTo(BigDecimal.valueOf(store.getMinOrderAmount())) < 0) {
            throw new BizException("minimum_order_amount");
        }

        BigDecimal deliveryFee = BigDecimal.valueOf(store.getDeliveryFee() == null ? 0 : store.getDeliveryFee());
        BigDecimal totalAmount = itemsAmount.add(deliveryFee);
        if (orderRequest.getTotalAmount() != null && orderRequest.getTotalAmount().compareTo(totalAmount) != 0) {
            // 화면에 보인 금액과 서버 계산이 다르면 기록만 남기고, 저장은 서버 계산 금액으로 한다
            log.warn("주문 금액 불일치 - orderId: {}, 요청: {}, 서버 계산: {}", orderId, orderRequest.getTotalAmount(), totalAmount);
        }

        OrderDto orderDto = OrderDto.builder()
                .orderId(orderId)
                .storeId(store.getId())
                .userId(userId)
                .totalAmount(totalAmount)
                .deliveryFee(deliveryFee)
                .orderRequest(orderRequest.getOrderRequest())
                .paymentMethod(orderRequest.getPaymentMethod())
                .status(OrderStatus.PENDING.name())
                .createdBy(userId)
                .createdDatetime(LocalDateTime.now())
                .build();

        DeliveryInfoDto deliveryInfo = DeliveryInfoDto.builder()
                .address(orderRequest.getDeliveryAddress().getAddress())
                .detailAddress(orderRequest.getDeliveryAddress().getDetailAddress())
                .phone(orderRequest.getDeliveryAddress().getPhone())
                .build();
        orderDto.setDeliveryInfo(deliveryInfo);

        orderDao.insertOrder(orderDto);
        for (OrderItemDto orderItem : orderItems) {
            orderItemDao.insertOrderItem(orderItem);
        }

        createOrderHistory(orderId, "ORDER_PLACED", "주문이 접수되었습니다.", userId);

        return OrderResponse.builder()
                .orderId(orderId)
                .status(OrderStatus.PENDING.name())
                .totalAmount(totalAmount)
                .message("주문이 성공적으로 접수되었습니다.")
                .estimatedTime("30분 - 40분")
                .build();
    }

    /**
     * 주문 항목 1개의 단가 = 메뉴 가격 + 선택한 옵션 추가금 합계 (해당 매장의 판매 중인 메뉴·옵션만 허용)
     */
    private BigDecimal getUnitPrice(Long storeId, OrderRequest.OrderItem item) {
        MenuDto menu = menuDao.findById(item.getMenuId());
        if (menu == null || !storeId.equals(menu.getStoreId())) {
            throw new BizException("menu_not_found");
        }
        if (Boolean.TRUE.equals(menu.getIsSoldOut())) {
            throw new BizException("menu_sold_out");
        }

        BigDecimal unitPrice = BigDecimal.valueOf(menu.getPrice() == null ? 0 : menu.getPrice());
        if (item.getOptions() == null || item.getOptions().isEmpty()) {
            return unitPrice;
        }

        Map<Long, MenuOptionDto> availableOptions = menuOptionDao.findByMenuId(menu.getId()).stream()
                .collect(Collectors.toMap(MenuOptionDto::getId, Function.identity()));
        for (OrderRequest.OrderItemOption option : item.getOptions()) {
            MenuOptionDto menuOption = availableOptions.get(option.getOptionId());
            if (menuOption == null) {
                throw new BizException("menu_option_not_found");
            }
            unitPrice = unitPrice.add(BigDecimal.valueOf(menuOption.getPriceChange() == null ? 0 : menuOption.getPriceChange()));
        }
        return unitPrice;
    }

    @Override
    public OrderConfirmResponse confirmOrder(OrderConfirmRequest confirmRequest, String userId) {
        // 주문 존재 여부 확인
        OrderDto order = orderDao.selectOrderById(confirmRequest.getOrderId());
        if (order == null) {
            throw new BizException("order_not_found");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BizException("permission_denied");
        }

        // 주문 상태 업데이트
        orderDao.updateOrderStatus(confirmRequest.getOrderId(), OrderStatus.CONFIRMED.name());

        // 주문 히스토리 생성
        createOrderHistory(confirmRequest.getOrderId(), "ORDER_CONFIRMED",
                "주문이 확인되었습니다. 예상 시간: " + confirmRequest.getEstimatedTime(), userId);

        return OrderConfirmResponse.builder()
                .orderId(confirmRequest.getOrderId())
                .status(OrderStatus.CONFIRMED.name())
                .estimatedTime(confirmRequest.getEstimatedTime())
                .message("주문이 확인되었습니다.")
                .build();

    }


    @Override
    @Transactional(readOnly = true)
    public ListResponseDto<OrderListResponse> getOrderList(String userId, int page, int limit, String status) {
        int offset = (page - 1) * limit;

        List<OrderListResponse> orders = orderDao.selectOrderList(userId, status, offset, limit);
        int totalCount = orderDao.selectOrderCount(userId, status);

        ListResponseDto<OrderListResponse> response = new ListResponseDto<>();
        response.setTotalCount(totalCount);
        response.setDataList(orders);

        return response;

    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String orderId, String userId) {
        OrderDetailResponse orderDetail = orderDao.selectOrderDetail(orderId);
        if (orderDetail == null) {
            throw new BizException("order_not_found");
        }

        if (!orderDetail.getUserId().equals(userId)) {
            throw new BizException("permission_denied");
        }

        // 주문 항목 조회
        List<OrderDetailResponse.OrderItem> items = orderItemDao.selectOrderItems(orderId);
        orderDetail.setItems(items);

        return orderDetail;

    }

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingResponse getOrderTracking(String orderId, String userId) {
        OrderDto order = orderDao.selectOrderById(orderId);
        if (order == null) {
            throw new BizException("order_not_found");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BizException("permission_denied");
        }

        // 주문 히스토리 조회
        List<OrderHistoryDto> histories = orderHistoryDao.selectOrderHistories(orderId);

        return OrderTrackingResponse.builder()
                .orderId(orderId)
                .currentStatus(order.getStatus())
                .histories(histories)
                .estimatedTime(getEstimatedTimeByStatus(order.getStatus()))
                .build();

    }

    @Override
    public OrderResponse cancelOrder(String orderId, OrderCancelRequest cancelRequest, String userId) {
        OrderDto order = orderDao.selectOrderById(orderId);
        if (order == null) {
            throw new BizException("order_not_found");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BizException("permission_denied");
        }

        // 취소 가능한 상태인지 확인
        OrderStatus currentStatus = OrderStatus.from(order.getStatus());
        if (currentStatus == null || !currentStatus.canCancel()) {
            throw new BizException("order_cannot_cancel");
        }

        // 주문 상태 업데이트
        orderDao.updateOrderStatus(orderId, OrderStatus.CANCELLED.name());

        // 주문 히스토리 생성
        createOrderHistory(orderId, "ORDER_CANCELLED",
                "주문이 취소되었습니다. 사유: " + cancelRequest.getReason(), userId);

        return OrderResponse.builder()
                .orderId(orderId)
                .status(OrderStatus.CANCELLED.name())
                .message("주문이 취소되었습니다.")
                .build();

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
        OrderStatus orderStatus = OrderStatus.from(status);
        return orderStatus == null ? "확인 중" : orderStatus.getEstimatedTime();
    }
}