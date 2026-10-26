package com.winter.winter_back.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description    :
 * fileName       : OrderListResponse
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
public class OrderListResponse {
    private String orderId;
    private String storeName;
    private String storeImage;
    private String orderType;
    private String status;
    private String statusDescription;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private LocalDateTime orderedAt;
    private String estimatedTime;
}