package com.winter.winter_back.domain.menu.dto;

import com.winter.winter_back.common.dto.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * description    : 메뉴 DTO
 * fileName       : MenuDto
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDto extends BaseDto {
    private Long id;
    private Long storeId;
    private Long categoryId;
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
    private List<MenuOptionGroupDto> optionGroups;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
