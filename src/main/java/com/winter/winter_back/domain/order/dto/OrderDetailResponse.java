package com.winter.winter_back.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * description    :
 * fileName       : OrderDetailResponse
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 * 26. 10. 26.       hj kim             주문 방식(orderType: 포장/배달) 추가
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    private String orderId;
    private String userId;
    private Long storeId;
    private String storeName;
    private String storePhone;
    private String storeAddress;
    private String status;
    private String statusDescription;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private String paymentMethod;
    private String orderType;
    private String orderRequest;
    private DeliveryInfoDto deliveryInfo;
    private List<OrderItem> items;
    private LocalDateTime orderedAt;
    private String estimatedTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long menuId;
        private String menuName;
        private String menuImage;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;
        private String specialRequest;
        private List<OrderItemOption> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemOption {
        private String optionName;
        private String optionValue;
        private BigDecimal additionalPrice;
    }
}