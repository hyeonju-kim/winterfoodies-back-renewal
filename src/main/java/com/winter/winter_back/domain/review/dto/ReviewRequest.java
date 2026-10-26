package com.winter.winter_back.domain.review.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * description    :
 * fileName       : ReviewRequest
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */

@Data
public class ReviewRequest {
    @NotNull(message = "매장 ID는 필수입니다.")
    private Long storeId;

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderOrderId;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private Long rating;

    @Size(max = 255, message = "리뷰 내용은 255자를 초과할 수 없습니다.")
    private String content;

    @Size(max = 255, message = "이미지 경로는 255자를 초과할 수 없습니다.")
    private String images;
}