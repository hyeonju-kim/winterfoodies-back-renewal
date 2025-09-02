package com.winter.winter_back.domain.review.dto;

import com.winter.winter_back.common.dto.ListResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * description    :
 * fileName       : ReviewListResponse
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewListResponse extends ListResponseDto<ReviewResponse> {
    private Double averageRating;    // 평균 평점
    private Long totalReviews;       // 전체 리뷰 수
}
