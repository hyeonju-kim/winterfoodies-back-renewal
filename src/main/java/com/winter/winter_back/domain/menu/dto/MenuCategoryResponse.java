package com.winter.winter_back.domain.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * description    : 메뉴 카테고리 응답 객체
 * fileName       : MenuCategoryResponse
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryResponse {
    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
    private Integer sortOrder;
    private List<MenuDto> menus;
}