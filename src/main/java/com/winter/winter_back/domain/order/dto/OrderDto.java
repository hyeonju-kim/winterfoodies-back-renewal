package com.winter.winter_back.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description    :
 * fileName       : OrderDto
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 * 26. 10. 26.       hj kim             주문 방식(orderType: 포장/배달) 추가
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long idx;
    private String orderId;
    private Long storeId;
    private String userId;
    private Long reviewId;
    private String processYn;
    private String message;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private String orderRequest;
    private String status;
    private String paymentMethod;
    private String orderType;
    private DeliveryInfoDto deliveryInfo;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private String createdBy;
    private String updatedBy;
}