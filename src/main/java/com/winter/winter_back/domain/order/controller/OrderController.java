package com.winter.winter_back.domain.order.controller;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.order.dto.*;
import com.winter.winter_back.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * description    :
 * fileName       : OrderController
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문하기
     */
    @PostMapping
    public OrderResponse createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        log.info("주문 요청: {}", orderRequest);

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return orderService.createOrder(orderRequest, loginUserInfo.getUserId());
    }

    /**
     * 주문 확인/완료
     */
    @PostMapping("/confirm")
    public OrderConfirmResponse confirmOrder(
            @Valid @RequestBody OrderConfirmRequest confirmRequest,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        log.info("주문 확인 요청: {}", confirmRequest);

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return orderService.confirmOrder(confirmRequest, loginUserInfo.getUserId());
    }

    /**
     * 주문 내역 조회
     */
    @GetMapping
    public ListResponseDto<OrderListResponse> getOrderList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "all") String status,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        log.info("주문 내역 조회 요청 - page: {}, limit: {}, status: {}", page, limit, status);

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return orderService.getOrderList(loginUserInfo.getUserId(), page, limit, status);
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrderDetail(
            @PathVariable String orderId,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        log.info("주문 상세 조회 요청 - orderId: {}", orderId);

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return orderService.getOrderDetail(orderId, loginUserInfo.getUserId());
    }

    /**
     * 주문 상태 추적
     */
    @GetMapping("/{orderId}/tracking")
    public OrderTrackingResponse getOrderTracking(
            @PathVariable String orderId,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        log.info("주문 상태 추적 요청 - orderId: {}", orderId);

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return orderService.getOrderTracking(orderId, loginUserInfo.getUserId());
    }

    /**
     * 주문 취소
     */
    @PutMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable String orderId,
            @Valid @RequestBody OrderCancelRequest cancelRequest,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        log.info("주문 취소 요청 - orderId: {}, reason: {}", orderId, cancelRequest.getReason());

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return orderService.cancelOrder(orderId, cancelRequest, loginUserInfo.getUserId());
    }
}
