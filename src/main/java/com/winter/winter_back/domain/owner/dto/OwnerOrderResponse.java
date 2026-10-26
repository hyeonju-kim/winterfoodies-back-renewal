package com.winter.winter_back.domain.owner.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description    : 사장님 주문 관리 - 주문 한 건 (목록·단건 공통)
 * fileName       : OwnerOrderResponse
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Data
public class OwnerOrderResponse {
    private String orderId;           // 주문번호
    private String status;            // 주문 상태
    private String orderType;         // PICKUP / DELIVERY
    private BigDecimal totalAmount;   // 결제 금액
    private String paymentMethod;     // 결제수단
    private String contactPhone;      // 손님 연락처
    private String orderRequest;      // 가게 요청사항
    private String itemSummary;       // 메뉴 요약 (예: 팥 붕어빵 x2, 슈크림 붕어빵 x1)
    private Integer itemCount;        // 메뉴 수량 합
    private LocalDateTime orderedAt;  // 주문일시
}
