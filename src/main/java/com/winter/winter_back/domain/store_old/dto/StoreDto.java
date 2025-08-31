package com.winter.winter_back.domain.store_old.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * description    :
 * fileName       : StoreDto
 * author         : hyeonju
 * date           : 25. 8. 25.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 25.        hyeonju             최초 생성
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreDto {
    private Long idx;                     // PK
    private Long storeId;                 // 가게 ID
    private Integer ownerId;              // 점주 ID
    private String storeAddressNo;        // 지번 주소
    private Long averageRating;           // 평균 평점
    private String storeBasicAddress;     // 기본 주소
    private String storeCloseTime;        // 마감 시간
    private String storeDetailAddress;    // 상세 주소
    private String estimatedCookingTime;  // 예상 조리 시간
    private String storeInfo;             // 가게 소개
    private Double latitude;              // 위도
    private Double longitude;             // 경도
    private String mapIcon;               // 지도 아이콘
    private String storeName;             // 가게명
    private String storeOfficialCodeNo;   // 사업자 등록 코드
    private String openDate;              // 오픈일
    private String storeOpenTime;         // 오픈 시간
    private String storeRoadCodeNo;       // 도로명 주소 코드
    private String status;                // 상태값 (예: 운영중/휴무 등)
    private String storeThumbnailImgUrl;  // 썸네일 이미지 URL

    private String categoryName; // 조인 결과로 카테고리명
}
