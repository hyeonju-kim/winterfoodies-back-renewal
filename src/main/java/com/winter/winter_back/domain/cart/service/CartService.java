package com.winter.winter_back.domain.cart.service;

import com.winter.winter_back.domain.cart.dto.CartItemRequest;
import com.winter.winter_back.domain.cart.dto.CartResponse;

/**
 * description    :
 * fileName       : CartService
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
public interface CartService {
    CartResponse getCart(String userId);
    CartResponse addToCart(String userId, CartItemRequest request);
    CartResponse updateCartItemQuantity(String userId, Long itemId, Integer quantity);
    CartResponse removeCartItem(String userId, Long itemId);
    void clearCart(String userId);
}
