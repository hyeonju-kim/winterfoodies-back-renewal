package com.winter.winter_back.domain.menu.service;

import com.winter.winter_back.domain.menu.dto.MenuDetailResponse;

/**
 * description    :
 * fileName       : MenuService
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
public interface MenuService {
    /**
     * 메뉴 상세 조회
     */
    MenuDetailResponse getMenuDetail(Long menuId);
}
