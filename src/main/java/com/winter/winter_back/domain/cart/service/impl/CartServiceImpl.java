package com.winter.winter_back.domain.cart.service.impl;

import com.winter.winter_back.common.exception.BusinessException;
import com.winter.winter_back.common.exception.ErrorCode;
import com.winter.winter_back.domain.cart.dao.CartDao;
import com.winter.winter_back.domain.cart.dao.CartItemDao;
import com.winter.winter_back.domain.cart.dto.*;
import com.winter.winter_back.domain.cart.service.CartService;
import com.winter.winter_back.domain.menu.dao.MenuDao;
import com.winter.winter_back.domain.menu.dao.MenuOptionDao;
import com.winter.winter_back.domain.menu.dto.MenuDto;
import com.winter.winter_back.domain.menu.dto.MenuOptionDto;
import com.winter.winter_back.domain.store.dao.StoreDao;
import com.winter.winter_back.domain.store.dto.StoreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description    :
 * fileName       : CartServiceImpl
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartDao cartDao;
    private final CartItemDao cartItemDao;
    private final MenuDao menuDao;
    private final MenuOptionDao menuOptionDao;
    private final StoreDao storeDao;

    @Override
    public CartResponse getCart(String userId) {
        CartDto cart = cartDao.findByUserId(userId);

        if (cart == null) {
            return CartResponse.builder()
                    .items(List.of())
                    .summary(CartSummaryResponse.builder()
                            .totalQuantity(0)
                            .itemsPrice(0)
                            .deliveryFee(0)
                            .totalPrice(0)
                            .minOrderAmount(0)
                            .remainingAmount(0)
                            .build())
                    .canOrder(false)
                    .message("장바구니가 비어있습니다")
                    .build();
        }

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(String userId, CartItemRequest request) {
        System.out.println("🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥");

        log.info("[addToCart] > [userId] === {}", userId);
        log.info("[addToCart] > [request] === {}", request);

        // 메뉴 정보 조회
        MenuDto menu = menuDao.findById(request.getMenuId());

        log.info("[addToCart] > [menu] === {}", menu);
        System.out.println("🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀");

        if (menu == null) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }

        // 매장 정보 조회
        StoreDto store = storeDao.findById(request.getStoreId());
        log.info("[addToCart] > [store] === {}", store);
        if (store == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        // 기존 장바구니 조회
        CartDto existingCart = cartDao.findByUserId(userId);

        log.info("[addToCart] > [existingCart] === {}", existingCart);
        System.out.println("👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍👍");


        // 다른 매장의 상품이 있는 경우 기존 장바구니 삭제
        if (existingCart != null && !existingCart.getStoreId().equals(request.getStoreId())) {
            cartItemDao.deleteByCartId(existingCart.getId());
            cartDao.deleteById(existingCart.getId());
            existingCart = null;
        }


        // 장바구니가 없으면 생성
        if (existingCart == null) {
            existingCart = CartDto.builder()
                    .userId(userId)
                    .storeId(request.getStoreId())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();


            cartDao.insert(existingCart);
            System.out.println("🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯🎯");
            log.info("[addToCart] > [cartId] === {}", existingCart.getId());

//            existingCart.setId(cartId);
            log.info("[addToCart] > [existingCart] === {}", existingCart);
        }

        // 옵션 가격 계산
        int optionPrice = calculateOptionPrice(request.getOptions());
        int itemTotalPrice = (menu.getPrice() + optionPrice) * request.getQuantity();

        // 장바구니 아이템 생성
        CartItemDto cartItem = CartItemDto.builder()
                .cartId(existingCart.getId())
                .menuId(request.getMenuId())
                .quantity(request.getQuantity())
                .totalPrice(itemTotalPrice)
                .specialRequest(request.getSpecialRequest())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("[addToCart] > [cartItem] === {}", cartItem);
        cartItemDao.insert(cartItem); // 여기서 에러나 ...

        log.info("[addToCart] > [cartItem] === {}", cartItem);
        System.out.println("🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎");
//        cartItem.setId(cartItemId);

        // 옵션 저장
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            saveCartItemOptions(cartItem.getId(), request.getOptions());
        }

        // 장바구니 업데이트 시간 갱신
        cartDao.updateUpdatedAt(existingCart.getId());

        // 최신 장바구니 조회하여 반환
        CartDto updatedCart = cartDao.findByUserId(userId);
        return buildCartResponse(updatedCart);
    }
    @Override
    @Transactional
    public CartResponse updateCartItemQuantity(String userId, Long itemId, Integer quantity) {
        // 장바구니 아이템 조회 및 권한 확인
        CartItemDto cartItem = cartItemDao.findById(itemId);
        if (cartItem == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        CartDto cart = cartDao.findById(cartItem.getCartId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CART_ACCESS_DENIED);
        }

        // 수량 및 총 가격 업데이트
        MenuDto menu = menuDao.findById(cartItem.getMenuId());
        List<CartItemOptionDto> options = cartItemDao.findOptionsByCartItemId(itemId);
        int optionPrice = options.stream().mapToInt(CartItemOptionDto::getOptionPrice).sum();
        int newTotalPrice = (menu.getPrice() + optionPrice) * quantity;

        cartItemDao.updateQuantityAndPrice(itemId, quantity, newTotalPrice);
        cartDao.updateUpdatedAt(cart.getId());

        // 최신 장바구니 조회하여 반환
        CartDto updatedCart = cartDao.findByUserId(userId);
        return buildCartResponse(updatedCart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(String userId, Long itemId) {
        // 장바구니 아이템 조회 및 권한 확인
        CartItemDto cartItem = cartItemDao.findById(itemId);
        if (cartItem == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        CartDto cart = cartDao.findById(cartItem.getCartId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CART_ACCESS_DENIED);
        }

        // 아이템 옵션 및 아이템 삭제
        cartItemDao.deleteOptionsByCartItemId(itemId);
        cartItemDao.deleteById(itemId);

        // 장바구니에 아이템이 없으면 장바구니도 삭제
        List<CartItemDto> remainingItems = cartItemDao.findByCartId(cart.getId());
        if (remainingItems.isEmpty()) {
            cartDao.deleteById(cart.getId());
            return CartResponse.builder()
                    .items(List.of())
                    .summary(CartSummaryResponse.builder()
                            .totalQuantity(0)
                            .itemsPrice(0)
                            .deliveryFee(0)
                            .totalPrice(0)
                            .build())
                    .canOrder(false)
                    .message("장바구니가 비어있습니다")
                    .build();
        }

        cartDao.updateUpdatedAt(cart.getId());

        // 최신 장바구니 조회하여 반환
        CartDto updatedCart = cartDao.findByUserId(userId);
        return buildCartResponse(updatedCart);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        CartDto cart = cartDao.findByUserId(userId);
        if (cart != null) {
            // 모든 아이템의 옵션 삭제
            List<CartItemDto> items = cartItemDao.findByCartId(cart.getId());
            for (CartItemDto item : items) {
                cartItemDao.deleteOptionsByCartItemId(item.getId());
            }

            // 모든 아이템 삭제
            cartItemDao.deleteByCartId(cart.getId());

            // 장바구니 삭제
            cartDao.deleteById(cart.getId());
        }
    }

    private CartResponse buildCartResponse(CartDto cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return CartResponse.builder()
                    .items(List.of())
                    .summary(CartSummaryResponse.builder()
                            .totalQuantity(0)
                            .itemsPrice(0)
                            .deliveryFee(0)
                            .totalPrice(0)
                            .build())
                    .canOrder(false)
                    .message("장바구니가 비어있습니다")
                    .build();
        }

        // 매장 정보 조회
        StoreDto store = storeDao.findById(cart.getStoreId());

        // 아이템 응답 변환
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());

        // 총계 계산
        int totalQuantity = cart.getItems().stream().mapToInt(CartItemDto::getQuantity).sum();
        int itemsPrice = cart.getItems().stream().mapToInt(CartItemDto::getTotalPrice).sum();
        int deliveryFee = store != null ? store.getDeliveryFee() : 0;
        int totalPrice = itemsPrice + deliveryFee;
        int minOrderAmount = store != null ? store.getMinOrderAmount() : 0;
        int remainingAmount = Math.max(0, minOrderAmount - itemsPrice);

        CartSummaryResponse summary = CartSummaryResponse.builder()
                .totalQuantity(totalQuantity)
                .itemsPrice(itemsPrice)
                .deliveryFee(deliveryFee)
                .totalPrice(totalPrice)
                .minOrderAmount(minOrderAmount)
                .remainingAmount(remainingAmount)
                .build();

        boolean canOrder = itemsPrice >= minOrderAmount;
        String message = canOrder ? null :
                String.format("최소 주문금액 %,d원 이상 주문해주세요", minOrderAmount);

        return CartResponse.builder()
                .cartId(cart.getId())
                .storeId(cart.getStoreId())
                .storeName(store != null ? store.getName() : null)
                .storeImage(store != null ? store.getImageUrl() : null)
                .items(itemResponses)
                .summary(summary)
                .canOrder(canOrder)
                .message(message)
                .build();
    }

    private CartItemResponse convertToItemResponse(CartItemDto item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .menuId(item.getMenuId())
                .menuName(item.getMenuName())
                .menuImage(item.getMenuImage())
                .menuPrice(item.getMenuPrice())
                .quantity(item.getQuantity())
                .itemTotalPrice(item.getTotalPrice())
                .specialRequest(item.getSpecialRequest())
                .options(item.getOptions())
                .build();
    }

    private int calculateOptionPrice(List<CartItemOptionRequest> options) {
        if (options == null || options.isEmpty()) {
            return 0;
        }

        return options.stream()
                .mapToInt(option -> {
                    MenuOptionDto menuOption = menuOptionDao.findById(option.getOptionId());
                    // priceChange를 사용 (기본 가격에서 추가/차감되는 금액)
                    return menuOption != null ? (menuOption.getPriceChange() != null ? menuOption.getPriceChange() : 0) : 0;
                })
                .sum();
    }

    private void saveCartItemOptions(Long cartItemId, List<CartItemOptionRequest> options) {
        System.out.println("👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀👀");

        log.info("[saveCartItemOptions] > [cartItemId] === {}", cartItemId);
        log.info("[saveCartItemOptions] > [options] === {}", options);
        System.out.println("🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈🎈");
        for (CartItemOptionRequest optionRequest : options) {
            MenuOptionDto menuOption = menuOptionDao.findById(optionRequest.getOptionId());
            System.out.println("🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀🍀");
            log.info("[saveCartItemOptions] > [menuOption] === {}", menuOption);

            if (menuOption != null) {
                CartItemOptionDto cartItemOption = CartItemOptionDto.builder()
                        .cartItemId(cartItemId)
                        .optionId(optionRequest.getOptionId())
                        .optionName(menuOption.getName())
                        .optionValue(optionRequest.getValue())
                        .optionPrice(menuOption.getPriceChange() != null ? menuOption.getPriceChange() : 0) // priceChange 사용
                        .build();

                log.info("[saveCartItemOptions] > [cartItemOption] === {}", cartItemOption);

                cartItemDao.insertOption(cartItemOption);
                log.info("[saveCartItemOptions] > [cartItemOption] ===****************** {}", cartItemOption);
            }
        }
    }
}
