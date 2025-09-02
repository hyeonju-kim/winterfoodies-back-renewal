package com.winter.winter_back.domain.favorite.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * description    :
 * fileName       : FavoriteResponse
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */
@Data
public class FavoriteResponse {
    private Long id;                    // 즐겨찾기 ID
    private Long storeId;               // 매장 ID
    private String storeName;           // 매장명
    private String storeDescription;    // 매장 설명
    private String category;            // 카테고리
    private String phone;               // 전화번호
    private String address;             // 주소
    private Double latitude;            // 위도
    private Double longitude;           // 경도
    private String status;              // 매장 상태
    private String imageUrl;            // 이미지 URL
    private Integer minOrderAmount;     // 최소 주문금액
    private Integer deliveryFee;        // 배달료
    private Integer deliveryTime;       // 배달 예상시간
    private String businessNumber;      // 사업자 등록번호
    private Boolean isFeatured;         // 추천 매장 여부
    private LocalDateTime createdAt;    // 즐겨찾기 등록일시
}