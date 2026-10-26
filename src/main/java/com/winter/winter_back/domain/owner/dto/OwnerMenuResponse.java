package com.winter.winter_back.domain.owner.dto;

import lombok.Data;

/**
 * description    : 사장님 메뉴 관리 - 메뉴 한 줄
 * fileName       : OwnerMenuResponse
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 * 26. 10. 26.       hj kim             메뉴 설명 추가 (메뉴 수정 화면용)
 */
@Data
public class OwnerMenuResponse {
    private Long menuId;          // 메뉴 ID
    private String categoryName;  // 메뉴 카테고리
    private String name;          // 메뉴명
    private String description;   // 메뉴 설명
    private Integer price;        // 판매가
    private String imageUrl;      // 사진
    private Boolean isSoldOut;    // 품절 여부
}
