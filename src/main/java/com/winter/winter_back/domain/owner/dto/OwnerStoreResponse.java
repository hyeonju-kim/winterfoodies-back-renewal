package com.winter.winter_back.domain.owner.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * description    : 사장님 홈 - 내 가게 정보와 오늘 현황
 * fileName       : OwnerStoreResponse
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 * 26. 10. 26.       hj kim             가게 소개·최소 주문금액 추가 (가게 정보 수정 화면용)
 */
@Data
public class OwnerStoreResponse {
    private Long storeId;                // 가게 ID
    private String name;                 // 가게명
    private String description;          // 가게 소개
    private Integer minOrderAmount;      // 최소 주문금액
    private String category;             // 간식 종류 코드
    private String address;              // 주소
    private String phone;                // 가게 전화
    private String imageUrl;             // 대표 이미지
    private String status;               // 영업 상태 (OPEN / TEMPORARILY_CLOSED / CLOSED)
    private Integer waitMinutes;         // 대기시간(분) - 손님 화면의 "조리 약 N분"
    private Integer todayOrderCount;     // 오늘 들어온 주문 수 (취소 제외)
    private BigDecimal todaySales;       // 오늘 매출 (받아간 주문 합계)
    private Integer newOrderCount;       // 새 주문 (접수 대기)
    private Integer inProgressOrderCount; // 처리 중 (확인·조리·픽업대기)
}
