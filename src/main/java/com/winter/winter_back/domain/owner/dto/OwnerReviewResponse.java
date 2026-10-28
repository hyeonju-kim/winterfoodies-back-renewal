package com.winter.winter_back.domain.owner.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * description    : 사장님 리뷰 관리 - 리뷰 한 건 (목록·답글 공통)
 * fileName       : OwnerReviewResponse
 * author         : hj kim
 * date           : 26. 10. 28.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 28.       hj kim             최초 생성
 */
@Data
public class OwnerReviewResponse {
    private Long reviewId;                 // 리뷰 ID
    private String orderId;                // 리뷰한 주문번호
    private String userName;               // 작성자 이름 (첫 글자만, 예: 김**)
    private Integer rating;                // 별점 (1~5)
    private String content;                // 리뷰 내용
    private String images;                 // 리뷰 사진 (쉼표 구분)
    private String itemSummary;            // 주문 메뉴 요약 (예: 팥 붕어빵 x2)
    private LocalDateTime createdDatetime; // 작성일시
    private String replyContent;           // 사장님 답글 (없으면 null)
    private LocalDateTime replyDatetime;   // 답글 일시
}
