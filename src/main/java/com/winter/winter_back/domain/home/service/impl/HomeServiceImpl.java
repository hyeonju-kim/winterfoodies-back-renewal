package com.winter.winter_back.domain.home.service.impl;

import com.winter.winter_back.domain.home.dto.HomeFeedResponse;
import com.winter.winter_back.domain.home.service.HomeService;
import com.winter.winter_back.domain.store.dao.StoreCustomDao;
import com.winter.winter_back.domain.store.dao.StoreDao;
import com.winter.winter_back.domain.store.dto.StoreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description    : 홈 서비스 구현체
 * fileName       : HomeServiceImpl
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HomeServiceImpl implements HomeService {

    private final StoreDao storeDao;
    private final StoreCustomDao storeCustomDao;

    @Override
    public HomeFeedResponse getHomeFeed(Double lat, Double lng) {
        // 추천 매장들
        List<StoreDto> recommendedStores = storeCustomDao.findRecommendedStores(lat, lng, 5);
        List<StoreDto> popularStores = storeCustomDao.findPopularStores(lat, lng, 5);
        List<StoreDto> nearbyStores = storeCustomDao.findNearbyStores(lat, lng, 5);
        List<StoreDto> newStores = storeCustomDao.findNewStores(lat, lng, 5);

        return HomeFeedResponse.builder()
                .recommendedStores(recommendedStores)
                .popularStores(popularStores)
                .nearbyStores(nearbyStores)
                .newStores(newStores)
                .build();
    }

}