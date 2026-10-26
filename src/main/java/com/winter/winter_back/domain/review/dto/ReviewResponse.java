package com.winter.winter_back.domain.review.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * description    :
 * fileName       : ReviewResponse
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */
@Data
public class ReviewResponse {
    private Long idx;                       // 리뷰 ID
    private Long storeId;                   // 매장 ID
    private String userId;                    // 사용자 ID
    private String orderOrderId;              // 주문 ID
    private Long rating;                    // 평점
    private String content;                 // 리뷰 내용
    private String images;                  // 리뷰 이미지
    private LocalDateTime createdDatetime;  // 작성일시
    private LocalDateTime modifiedDatetime; // 수정일시

    // 매장 정보
    private String storeName;               // 매장명
    private String storeCategory;           // 매장 카테고리
    private String storeImageUrl;           // 매장 이미지

    // 사용자 정보
    private String userName;                // 사용자명
}