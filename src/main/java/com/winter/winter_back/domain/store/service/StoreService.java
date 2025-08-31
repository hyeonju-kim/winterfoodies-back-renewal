package com.winter.winter_back.domain.store.service;

import com.winter.winter_back.domain.home.dto.MenuListResponse;
import com.winter.winter_back.domain.store.dto.StoreDetailResponse;
import com.winter.winter_back.domain.store.dto.StoreListResponse;
import com.winter.winter_back.domain.store.dto.StoreRankingResponse;
import com.winter.winter_back.domain.store.dto.StoreSearchRequest;

/**
 * description    : 매장 서비스 인터페이스
 * fileName       : StoreService
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
public interface StoreService {
    StoreListResponse getStores(StoreSearchRequest request);
    StoreListResponse searchStores(StoreSearchRequest request);
    StoreDetailResponse getStoreDetail(Long storeId, Double lat, Double lng);
    MenuListResponse getStoreMenu(Long storeId);
    StoreRankingResponse getStoreRankings(String type, Double lat, Double lng, int limit);
}
