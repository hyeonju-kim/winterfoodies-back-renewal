package com.winter.winter_back.domain.review.service;

import com.winter.winter_back.domain.review.dto.*;

/**
 * description    :
 * fileName       : ReviewService
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */

public interface ReviewService {

    /**
     * 매장 리뷰 목록 조회
     * @param storeId 매장 ID
     * @param page 페이지 번호
     * @param limit 페이지 크기
     * @return 리뷰 목록
     */
    ReviewListResponse getStoreReviews(Long storeId, int page, int limit);

    /**
     * 사용자 리뷰 목록 조회
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param limit 페이지 크기
     * @return 리뷰 목록
     */
    ReviewListResponse getUserReviews(String userId, int page, int limit);

    /**
     * 리뷰 상세 조회
     * @param reviewId 리뷰 ID
     * @return 리뷰 상세 정보
     */
    ReviewResponse getReview(Long reviewId);

    /**
     * 리뷰 작성
     * @param userId 사용자 ID
     * @param request 리뷰 작성 요청
     */
    void createReview(String userId, ReviewRequest request);

    /**
     * 리뷰 수정
     * @param userId 사용자 ID
     * @param reviewId 리뷰 ID
     * @param request 리뷰 수정 요청
     */
    void updateReview(String userId, Long reviewId, ReviewUpdateRequest request);

    /**
     * 리뷰 삭제
     * @param userId 사용자 ID
     * @param reviewId 리뷰 ID
     */
    void deleteReview(String userId, Long reviewId);

    /**
     * 매장 리뷰 통계 조회
     * @param storeId 매장 ID
     * @return 리뷰 통계
     */
    ReviewStatsResponse getStoreReviewStats(Long storeId);

    /**
     * 리뷰 작성 가능 여부 확인
     * @param userId 사용자 ID
     * @param orderOrderId 주문 ID
     * @return 작성 가능 여부
     */
    boolean isReviewWritable(String userId, Long orderOrderId);
}