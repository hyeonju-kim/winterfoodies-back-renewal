package com.winter.winter_back.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * description    :
 * fileName       : CartItemDto
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
public class CartItemDto {
    private Long id;
    private Long cartId;
    private Long menuId;
    private String menuName;
    private String menuImage;
    private Integer menuPrice;
    private Integer quantity;
    private Integer totalPrice;
    private String specialRequest;
    private List<CartItemOptionDto> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}