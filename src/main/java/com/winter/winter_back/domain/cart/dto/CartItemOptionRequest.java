package com.winter.winter_back.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * description    :
 * fileName       : CartItemOptionRequest
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.         hj kim             최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemOptionRequest {
    @NotNull(message = "옵션 ID는 필수입니다")
    private Long optionId;

    @NotNull(message = "옵션 값은 필수입니다")
    private String value;
}