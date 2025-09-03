package com.winter.winter_back.domain.menu.controller;

import com.winter.winter_back.domain.menu.dto.MenuDetailResponse;
import com.winter.winter_back.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description    :
 * fileName       : MenuController
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Slf4j
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 메뉴 상세 정보 조회
     */
    @GetMapping("/{menuId}")
    public MenuDetailResponse getMenuDetail(@PathVariable Long menuId) {
        log.info("[getMenuDetail] 메뉴 상세 조회 요청 - menuId: {}", menuId);

        return menuService.getMenuDetail(menuId);
    }
}