package com.winter.winter_back.domain.review.controller;

import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.review.dto.*;
import com.winter.winter_back.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * description    :
 * fileName       : ReviewController
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 매장 리뷰 목록 조회
     * GET /reviews/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ReviewListResponse getStoreReviews(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("매장 리뷰 목록 조회 - storeId: {}, page: {}, limit: {}", storeId, page, limit);

        return reviewService.getStoreReviews(storeId, page, limit);
    }

    /**
     * 내 리뷰 목록 조회
     * GET /reviews/my
     */
    @GetMapping("/my")
    public ReviewListResponse getMyReviews(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("내 리뷰 목록 조회 - userId: {}, page: {}, limit: {}", loginUser.getUserId(), page, limit);

        return reviewService.getUserReviews(loginUser.getUserId(), page, limit);
    }

    /**
     * 리뷰 상세 조회
     * GET /reviews/{reviewId}
     */
    @GetMapping("/{reviewId}")
    public ReviewResponse getReview(@PathVariable Long reviewId) {

        log.info("리뷰 상세 조회 - reviewId: {}", reviewId);

        return reviewService.getReview(reviewId);
    }

    /**
     * 리뷰 작성
     * POST /reviews
     */
    @PostMapping
    public String createReview(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUser,
            @Valid @RequestBody ReviewRequest request) {

        log.info("리뷰 작성 - userId: {}, storeId: {}, orderOrderId: {}, rating: {}",
                loginUser.getUserId(), request.getStoreId(), request.getOrderOrderId(), request.getRating());

        reviewService.createReview(loginUser.getUserId(), request);

        return "리뷰가 작성되었습니다.";
    }

    /**
     * 리뷰 수정
     * PUT /reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    public String updateReview(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUser,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {

        log.info("리뷰 수정 - userId: {}, reviewId: {}, rating: {}",
                loginUser.getUserId(), reviewId, request.getRating());

        reviewService.updateReview(loginUser.getUserId(), reviewId, request);

        return "리뷰가 수정되었습니다.";
    }

    /**
     * 리뷰 삭제
     * DELETE /reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    public String deleteReview(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUser,
            @PathVariable Long reviewId) {

        log.info("리뷰 삭제 - userId: {}, reviewId: {}", loginUser.getUserId(), reviewId);

        reviewService.deleteReview(loginUser.getUserId(), reviewId);

        return "리뷰가 삭제되었습니다.";
    }

    /**
     * 매장 리뷰 통계 조회
     * GET /reviews/store/{storeId}/stats
     */
    @GetMapping("/store/{storeId}/stats")
    public ReviewStatsResponse getStoreReviewStats(@PathVariable Long storeId) {

        log.info("매장 리뷰 통계 조회 - storeId: {}", storeId);

        return reviewService.getStoreReviewStats(storeId);
    }

    /**
     * 주문 리뷰 작성 가능 여부 확인
     * GET /reviews/order/{orderOrderId}/writable
     */
    @GetMapping("/order/{orderOrderId}/writable")
    public Boolean isReviewWritable(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUser,
            @PathVariable Long orderOrderId) {

        log.info("리뷰 작성 가능 여부 확인 - userId: {}, orderOrderId: {}", loginUser.getUserId(), orderOrderId);

        return reviewService.isReviewWritable(loginUser.getUserId(), orderOrderId);
    }
}
