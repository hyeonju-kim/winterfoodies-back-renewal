package com.winter.winter_back.domain.review.dto;

/**
 * description    : 리뷰 사진 올리기 응답 (서버 주소를 뺀 사진 경로)
 * fileName       : ReviewImageResponse
 * author         : hj kim
 * date           : 26. 10. 27.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 27.       hj kim             최초 생성
 */
public record ReviewImageResponse(String imageUrl) {
}
