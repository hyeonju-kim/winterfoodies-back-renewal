package com.winter.winter_back.domain.category.dao;

import com.winter.winter_back.domain.category.dto.CategoryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * description    :
 * fileName       : CategoryDao
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */
@Mapper
public interface CategoryDao {

    /**
     * 모든 활성화된 메뉴 카테고리 조회 (tb_menu_category 테이블 사용)
     */
    List<CategoryDto> selectAllActiveMenuCategories();

    /**
     * 카테고리 ID로 조회
     */
    CategoryDto selectMenuCategoryById(Long categoryId);
}