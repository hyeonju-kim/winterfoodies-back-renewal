package com.winter.winter_back.domain.review.dto;

import lombok.Data;

/**
 * description    :
 * fileName       : ReviewStatsResponse
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */
@Data
public class ReviewStatsResponse {
    private Long storeId;               // 매장 ID
    private Long totalReviews;          // 전체 리뷰 수
    private Double averageRating;       // 평균 평점
    private Long rating5Count;          // 5점 리뷰 수
    private Long rating4Count;          // 4점 리뷰 수
    private Long rating3Count;          // 3점 리뷰 수
    private Long rating2Count;          // 2점 리뷰 수
    private Long rating1Count;          // 1점 리뷰 수
}
