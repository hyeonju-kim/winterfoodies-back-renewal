package com.winter.winter_back.domain.store.controller;

import com.winter.winter_back.domain.home.dto.MenuListResponse;
import com.winter.winter_back.domain.store.dto.*;
import com.winter.winter_back.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * description    : 매장 컨트롤러
 * fileName       : StoreController
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {
    private final StoreService storeService;

    /**
     * 매장 목록 조회
     */
    @GetMapping
    public StoreListResponse getStores(@ModelAttribute StoreSearchRequest request) {
        return storeService.getStores(request);
    }


    /**
     * 매장 검색
     */
    @GetMapping("/search")
    public StoreListResponse searchStores(@ModelAttribute StoreSearchRequest request) {
        return storeService.searchStores(request);
    }

    /**
     * 매장 상세 조회
     */
    @GetMapping("/{storeId}")
    public StoreDetailResponse getStoreDetail(
            @PathVariable Long storeId,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {

        return storeService.getStoreDetail(storeId, lat, lng);
    }

    /**
     * 매장 메뉴 조회
     */
    @GetMapping("/{storeId}/menu")
    public MenuListResponse getStoreMenu(@PathVariable Long storeId) {
        return storeService.getStoreMenu(storeId);
    }

    /**
     * 인기 매장 랭킹
     */
    @GetMapping("/rankings")
    public StoreRankingResponse getStoreRankings(@ModelAttribute StoreRankingRequest request) {
        return storeService.getStoreRankings(request.getType(), request.getLat(), request.getLng(), request.getLimit());
    }
}
