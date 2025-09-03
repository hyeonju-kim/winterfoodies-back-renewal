package com.winter.winter_back.domain.category.service;

import com.winter.winter_back.domain.category.dto.CategoryDto;
import com.winter.winter_back.domain.category.dto.CategoryListResponse;

/**
 * description    :
 * fileName       : CategoryService
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */

public interface CategoryService {

    /**
     * 모든 활성화된 카테고리 조회
     */
    CategoryListResponse getAllActiveCategories();

    /**
     * 카테고리 ID로 조회
     */
    CategoryDto getCategoryById(Long categoryId);
}