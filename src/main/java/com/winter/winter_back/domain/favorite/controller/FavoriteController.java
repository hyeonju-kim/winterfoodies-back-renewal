package com.winter.winter_back.domain.favorite.controller;

import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.favorite.dto.FavoriteListResponse;
import com.winter.winter_back.domain.favorite.dto.FavoriteRequest;
import com.winter.winter_back.domain.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * description    :
 * fileName       : FavoriteController
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */



@Slf4j
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 즐겨찾기 목록 조회
     * GET /favorites
     */
    @GetMapping
    public FavoriteListResponse getFavoriteList(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUser) {

        log.info("즐겨찾기 목록 조회 - userId: {}", loginUser.getUserId());

        return favoriteService.getFavoriteList(loginUser.getUserId());
    }

    /**
     * 즐겨찾기 추가
     * POST /favorites
     */
    @PostMapping
    public void addFavorite(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUser,
            @Valid @RequestBody FavoriteRequest request) {

        log.info("즐겨찾기 추가 - userId: {}, storeId: {}", loginUser.getUserId(), request.getStoreId());

        favoriteService.addFavorite(loginUser.getUserId(), request.getStoreId());
    }

    /**
     * 즐겨찾기 삭제
     * DELETE /favorites/{storeId}
     */
    @DeleteMapping("/{storeId}")
    public void removeFavorite(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUser,
            @PathVariable Long storeId) {

        log.info("즐겨찾기 삭제 - userId: {}, storeId: {}", loginUser.getUserId(), storeId);

        favoriteService.removeFavorite(loginUser.getUserId(), storeId);
    }

    /**
     * 즐겨찾기 여부 확인
     * GET /favorites/check/{storeId}
     */
    @GetMapping("/check/{storeId}")
    public Boolean checkFavorite(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUser,
            @PathVariable Long storeId) {

        log.info("즐겨찾기 여부 확인 - userId: {}, storeId: {}", loginUser.getUserId(), storeId);

        return favoriteService.isFavorite(loginUser.getUserId(), storeId);
    }
}