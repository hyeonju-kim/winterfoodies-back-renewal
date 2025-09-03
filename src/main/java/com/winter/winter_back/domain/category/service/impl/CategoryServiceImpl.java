package com.winter.winter_back.domain.category.service.impl;

import com.winter.winter_back.domain.category.dao.CategoryDao;
import com.winter.winter_back.domain.category.dto.CategoryDto;
import com.winter.winter_back.domain.category.dto.CategoryListResponse;
import com.winter.winter_back.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * description    :
 * fileName       : CategoryServiceImpl
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    @Override
    public CategoryListResponse getAllActiveCategories() {
        log.info("[getAllActiveCategories] 활성화된 메뉴 카테고리 전체 조회 시작");

        List<CategoryDto> categories = categoryDao.selectAllActiveMenuCategories();

        log.info("[getAllActiveCategories] 조회된 카테고리 수: {}", categories.size());

        return new CategoryListResponse(categories);
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        log.info("[getCategoryById] 메뉴 카테고리 조회 시작 - categoryId: {}", categoryId);

        CategoryDto category = categoryDao.selectMenuCategoryById(categoryId);

        if (category == null) {
            throw new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + categoryId);
        }

        log.info("[getCategoryById] 카테고리 조회 완료 - {}", category.getCategoryName());

        return category;
    }
}