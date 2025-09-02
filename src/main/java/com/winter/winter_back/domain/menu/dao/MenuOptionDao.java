package com.winter.winter_back.domain.menu.dao;

import com.winter.winter_back.domain.menu.dto.MenuOptionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    :
 * fileName       : MenuOptionDao
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Mapper
public interface MenuOptionDao {
    MenuOptionDto findById(Long id);
    List<MenuOptionDto> findByOptionGroupId(Long optionGroupId);
    List<MenuOptionDto> findByMenuId(Long menuId);
    Integer getPrice(Long optionId);
    Long insert(MenuOptionDto menuOption);
    void update(MenuOptionDto menuOption);
    void deleteById(Long id);
    void deleteByOptionGroupId(Long optionGroupId);
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}
