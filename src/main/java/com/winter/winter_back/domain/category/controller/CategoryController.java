package com.winter.winter_back.domain.category.controller;

import com.winter.winter_back.domain.category.dto.CategoryListResponse;
import com.winter.winter_back.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description    :
 * fileName       : CategoryController
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 메뉴 카테고리 목록 조회
     */
    @GetMapping
    public CategoryListResponse getCategories() {
        log.info("[getCategories] 카테고리 목록 조회 요청");

        return categoryService.getAllActiveCategories();
    }
}