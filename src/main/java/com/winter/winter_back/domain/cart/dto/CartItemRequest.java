package com.winter.winter_back.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * description    :
 * fileName       : CartItemRequest
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
public class CartItemRequest {
    @NotNull(message = "메뉴 ID는 필수입니다")
    private Long menuId;

    @NotNull(message = "매장 ID는 필수입니다")
    private Long storeId;

    @NotNull(message = "수량은 필수입니다")
    @Positive(message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;

    private String specialRequest;

    private List<CartItemOptionRequest> options;
}