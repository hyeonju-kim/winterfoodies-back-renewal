package com.winter.winter_back.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * description    :
 * fileName       : CartQuantityUpdateRequest
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
public class CartQuantityUpdateRequest {
    @NotNull(message = "수량은 필수입니다")
    @Positive(message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;
}