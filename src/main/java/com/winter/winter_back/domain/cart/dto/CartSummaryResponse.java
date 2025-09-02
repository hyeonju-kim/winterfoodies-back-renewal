package com.winter.winter_back.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description    :
 * fileName       : CartSummaryResponse
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartSummaryResponse {
    private Integer totalQuantity;
    private Integer itemsPrice;
    private Integer deliveryFee;
    private Integer totalPrice;
    private Integer minOrderAmount;
    private Integer remainingAmount;
}