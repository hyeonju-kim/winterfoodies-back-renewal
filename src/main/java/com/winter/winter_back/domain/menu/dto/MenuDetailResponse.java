package com.winter.winter_back.domain.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description    :
 * fileName       : MenuDetailResponse
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDetailResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private Integer price;
    private Integer originalPrice;
    private String imageUrl;
    private Boolean isPopular;
    private Boolean isRecommended;
    private Boolean isSoldOut;
    private Boolean isActive;
    private Integer sortOrder;
    private Integer calories;
    private String ingredients;
    private String allergens;
    private Integer spiceLevel;
    private Integer preparationTime;
}