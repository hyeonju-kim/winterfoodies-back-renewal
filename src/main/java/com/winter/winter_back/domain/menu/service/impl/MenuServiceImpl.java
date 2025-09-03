package com.winter.winter_back.domain.menu.service.impl;

import com.winter.winter_back.domain.menu.dao.MenuDao;
import com.winter.winter_back.domain.menu.dto.MenuDetailResponse;
import com.winter.winter_back.domain.menu.dto.MenuDto;
import com.winter.winter_back.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description    :
 * fileName       : MenuServiceImpl
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {

    private final MenuDao menuDao;

    @Override
    public MenuDetailResponse getMenuDetail(Long menuId) {
        log.info("[getMenuDetail] 메뉴 상세 조회 시작 - menuId: {}", menuId);

        MenuDto menu = menuDao.selectMenuById(menuId);

        if (menu == null) {
            throw new IllegalArgumentException("메뉴를 찾을 수 없습니다. ID: " + menuId);
        }

        MenuDetailResponse response = MenuDetailResponse.builder()
                .id(menu.getId())
                .storeId(menu.getStoreId())
                .storeName(menu.getName())
                .categoryId(menu.getCategoryId())
                .categoryName(menu.getCategoryName())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .originalPrice(menu.getOriginalPrice())
                .imageUrl(menu.getImageUrl())
                .isPopular(menu.getIsPopular())
                .isRecommended(menu.getIsRecommended())
                .isSoldOut(menu.getIsSoldOut())
                .isActive(menu.getIsActive())
                .sortOrder(menu.getSortOrder())
                .calories(menu.getCalories())
                .ingredients(menu.getIngredients())
                .allergens(menu.getAllergens())
                .spiceLevel(menu.getSpiceLevel())
                .preparationTime(menu.getPreparationTime())
                .build();

        log.info("[getMenuDetail] 메뉴 상세 조회 완료 - {}", menu.getName());

        return response;
    }
}