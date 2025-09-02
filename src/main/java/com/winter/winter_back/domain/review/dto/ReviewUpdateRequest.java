package com.winter.winter_back.domain.review.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * description    :
 * fileName       : ReviewUpdateRequest
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */
@Data
public class ReviewUpdateRequest {
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private Long rating;

    @Size(max = 255, message = "리뷰 내용은 255자를 초과할 수 없습니다.")
    private String content;

    @Size(max = 255, message = "이미지 경로는 255자를 초과할 수 없습니다.")
    private String images;
}
