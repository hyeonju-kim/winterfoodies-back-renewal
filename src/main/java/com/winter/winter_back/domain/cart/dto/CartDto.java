package com.winter.winter_back.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * description    :
 * fileName       : CartDto
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
public class CartDto {
    private Long id;
    private String userId;
    private Long storeId;
    private String storeName;
    private String storeImage;
    private List<CartItemDto> items;
    private Integer totalQuantity;
    private Integer totalPrice;
    private Integer deliveryFee;
    private Integer minOrderAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}