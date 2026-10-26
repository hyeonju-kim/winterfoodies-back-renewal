package com.winter.winter_back.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * description    :
 * fileName       : OrderConfirmRequest
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmRequest {
    @NotBlank(message = "주문 ID는 필수입니다")
    private String orderId;

    private String estimatedTime;
}
