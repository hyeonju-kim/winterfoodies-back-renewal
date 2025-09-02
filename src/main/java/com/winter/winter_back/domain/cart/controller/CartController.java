package com.winter.winter_back.domain.cart.controller;

import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.cart.dto.CartItemRequest;
import com.winter.winter_back.domain.cart.dto.CartQuantityUpdateRequest;
import com.winter.winter_back.domain.cart.dto.CartResponse;
import com.winter.winter_back.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * description    :
 * fileName       : CartController
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 조회
     */
    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {
        log.info("[getCart] > [userId] === {}", loginUserInfo.getUserId());

        return cartService.getCart(loginUserInfo.getUserId());
    }

    /**
     * 장바구니 담기
     */
    @PostMapping("/items")
    public CartResponse addToCart(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
            @Valid @RequestBody CartItemRequest request) {
        log.info("[addToCart] > [userId] === {}, [request] === {}",
                loginUserInfo.getUserId(), request);

        System.out.println("👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍");


        return cartService.addToCart(loginUserInfo.getUserId(), request);
    }

    /**
     * 장바구니 수량 변경
     */
    @PutMapping("/items/{itemId}")
    public CartResponse updateCartItemQuantity(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
            @PathVariable Long itemId,
            @Valid @RequestBody CartQuantityUpdateRequest request) {
        log.info("[updateCartItemQuantity] > [userId] === {}, [itemId] === {}, [request] === {}",
                loginUserInfo.getUserId(), itemId, request);

        return cartService.updateCartItemQuantity(
                loginUserInfo.getUserId(), itemId, request.getQuantity());
    }

    /**
     * 장바구니 항목 삭제
     */
    @DeleteMapping("/items/{itemId}")
    public CartResponse removeCartItem(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
            @PathVariable Long itemId) {
        log.info("[removeCartItem] > [userId] === {}, [itemId] === {}",
                loginUserInfo.getUserId(), itemId);

        return cartService.removeCartItem(loginUserInfo.getUserId(), itemId);
    }

    /**
     * 장바구니 전체 삭제
     */
    @DeleteMapping
    public void clearCart(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {
        log.info("[clearCart] > [userId] === {}", loginUserInfo.getUserId());

        cartService.clearCart(loginUserInfo.getUserId());
    }
}