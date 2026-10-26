package com.winter.winter_back.domain.store.service.impl;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.home.dto.MenuListResponse;
import com.winter.winter_back.domain.menu.dao.MenuDao;
import com.winter.winter_back.domain.menu.dto.MenuCategoryResponse;
import com.winter.winter_back.domain.menu.dto.MenuDto;
import com.winter.winter_back.domain.store.dao.StoreCustomDao;
import com.winter.winter_back.domain.store.dao.StoreDao;
import com.winter.winter_back.domain.store.dto.*;
import com.winter.winter_back.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * description    : 매장 서비스 구현체
 * fileName       : StoreServiceImpl
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreDao storeDao;
    private final MenuDao menuDao;
    private final StoreCustomDao storeCustomDao;

    @Override
    public StoreListResponse getStores(StoreSearchRequest request) {
        // 거리 계산 및 정렬
        List<StoreDto> stores = storeCustomDao.findStoresWithDistance(request);
        long totalCount = storeCustomDao.countStoresWithDistance(request);

        return StoreListResponse.builder()
                .stores(stores)
                .totalCount(totalCount)
                .currentPage(request.getPage())
                .totalPages((int) Math.ceil((double) totalCount / request.getSize()))
                .build();
    }

    @Override
    public StoreListResponse searchStores(StoreSearchRequest request) {
        List<StoreDto> stores = storeCustomDao.searchStoresWithDistance(request);
        long totalCount = storeCustomDao.countSearchStoresWithDistance(request);

        return StoreListResponse.builder()
                .stores(stores)
                .totalCount(totalCount)
                .currentPage(request.getPage())
                .totalPages((int) Math.ceil((double) totalCount / request.getSize()))
                .build();
    }

    @Override
    public StoreDetailResponse getStoreDetail(Long storeId, Double lat, Double lng) {
        StoreDto store = storeDao.findById(storeId);

        if (store == null) {
            throw new BizException("매장을 찾을 수 없습니다.");
        }

        // 거리 계산
        Double distance = null;
        if (lat != null && lng != null) {
            distance = calculateDistance(lat, lng, store.getLatitude(), store.getLongitude());
        }

        List<StoreHoursDto> hours = storeDao.findStoreHours(storeId);
        List<StoreCategoryDto> categories = storeDao.findStoreCategories(storeId);

        return StoreDetailResponse.builder()
                .store(store)
                .distance(distance)
                .hours(hours)
                .categories(categories)
                .build();
    }

    // StoreServiceImpl.java 수정
    @Override
    public MenuListResponse getStoreMenu(Long storeId) {
        // 매장 존재 확인
        StoreDto store = storeDao.findById(storeId);
        if (store == null) {
            throw new BizException("매장을 찾을 수 없습니다.");
        }

        // 메뉴 카테고리 조회
        List<MenuCategoryResponse> categories = storeDao.findMenusByStore(storeId);

        // 각 카테고리별 메뉴들 조회
        for (MenuCategoryResponse category : categories) {
            List<MenuDto> menus = menuDao.findMenusByCategory(category.getCategoryId());
            category.setMenus(menus);
        }

        return MenuListResponse.builder()
                .storeId(storeId)
                .storeName(store.getName())
                .categories(categories)
                .build();
    }

    @Override
    public StoreRankingResponse getStoreRankings(String type, Double lat, Double lng, int limit) {
        List<StoreDto> stores;

        // 랭킹 종류를 지정하지 않으면 인기순
        String rankingType = StringUtils.hasText(type) ? type.trim().toLowerCase() : "popular";

        switch (rankingType) {
            case "popular":
                stores = storeCustomDao.findPopularStores(lat, lng, limit);
                break;
            case "rating":
                stores = storeCustomDao.findHighRatedStores(lat, lng, limit);
                break;
            case "new":
                stores = storeCustomDao.findNewStores(lat, lng, limit);
                break;
            case "discount":
                stores = storeCustomDao.findDiscountStores(lat, lng, limit);
                break;
            default:
                throw new BizException("code_not_found", "랭킹 종류(" + type + ")");
        }

        return StoreRankingResponse.builder()
                .type(rankingType)
                .stores(stores)
                .build();
    }

    // 거리 계산 (Haversine formula)
    private Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        final int R = 6371; // 지구 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // km 단위
    }
}