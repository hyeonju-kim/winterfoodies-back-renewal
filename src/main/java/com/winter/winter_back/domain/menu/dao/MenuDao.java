package com.winter.winter_back.domain.menu.dao;

import com.winter.winter_back.domain.menu.dto.MenuDto;
import com.winter.winter_back.domain.menu.dto.MenuOptionDto;
import com.winter.winter_back.domain.menu.dto.MenuOptionGroupDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    : 메뉴 DAO
 * fileName       : MenuDao
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Mapper
public interface MenuDao {

    MenuDto findById(Long id);
    List<MenuDto> findByStoreId(Long storeId);
    List<MenuDto> findByCategoryId(Long categoryId);
    List<MenuDto> findByStoreIdAndCategoryId(@Param("storeId") Long storeId,
                                             @Param("categoryId") Long categoryId);
    Long insert(MenuDto menu);
    void update(MenuDto menu);
    void deleteById(Long id);
    void updateStatus(@Param("id") Long id, @Param("status") String status);


    // 카테고리별 메뉴 조회
    List<MenuDto> findMenusByCategory(@Param("categoryId") Long categoryId);

    // 메뉴별 옵션 그룹 조회
    List<MenuOptionGroupDto> findOptionGroupsByMenu(@Param("menuId") Long menuId);

    // 옵션 그룹별 옵션 조회
    List<MenuOptionDto> findOptionsByGroup(@Param("optionGroupId") Long optionGroupId);

    // 매장의 모든 메뉴 조회 (카테고리 포함)
    List<MenuDto> findMenusByStore(@Param("storeId") Long storeId);

    // 인기 메뉴 조회
    List<MenuDto> findPopularMenusByStore(@Param("storeId") Long storeId);

    // 추천 메뉴 조회
    List<MenuDto> findRecommendedMenusByStore(@Param("storeId") Long storeId);

    // 메뉴 상세 조회 (옵션 포함)
    MenuDto findMenuWithOptions(@Param("menuId") Long menuId);

    // 메뉴 검색
    List<MenuDto> searchMenus(@Param("storeId") Long storeId, @Param("keyword") String keyword);

    // CRUD
    void insertMenu(MenuDto menu);
    void updateMenu(MenuDto menu);
    void deleteMenu(@Param("menuId") Long menuId);

    // 메뉴 상태 변경
    void updateMenuStatus(@Param("menuId") Long menuId, @Param("isActive") Boolean isActive);
    void updateMenuSoldOut(@Param("menuId") Long menuId, @Param("isSoldOut") Boolean isSoldOut);
}