package com.winter.winter_back.domain.product_old.dto;

import lombok.Data;

/**
 * description    :
 * fileName       : ProductDto
 * author         : hyeonju
 * date           : 25. 8. 25.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 25.        hyeonju             최초 생성
 */
@Data
public class ProductDto {
    private Long menuId;     // 메뉴 PK
    private Long storeId;    // 상점 ID (FK)
    private String menuName; // 메뉴 이름
    private Integer price;   // 메뉴 가격
    private Boolean bestYn;  // 인기 여부
}
