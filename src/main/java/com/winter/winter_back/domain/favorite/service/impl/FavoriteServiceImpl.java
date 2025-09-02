package com.winter.winter_back.domain.favorite.service.impl;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.favorite.dao.FavoriteDao;
import com.winter.winter_back.domain.favorite.dto.FavoriteDto;
import com.winter.winter_back.domain.favorite.dto.FavoriteListResponse;
import com.winter.winter_back.domain.favorite.dto.FavoriteResponse;
import com.winter.winter_back.domain.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * description    :
 * fileName       : FavoriteServiceImpl
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteDao favoriteDao;

    @Override
    public FavoriteListResponse getFavoriteList(String userId) {
        List<FavoriteDto> favorites = favoriteDao.selectFavoriteListByUserId(userId);

        List<FavoriteResponse> favoriteResponses = favorites.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        FavoriteListResponse response = new FavoriteListResponse();
        response.setDataList(favoriteResponses);
        response.setTotalCount(favoriteResponses.size());

        return response;
    }

    @Override
    @Transactional
    public void addFavorite(String userId, Long storeId) {
        // 이미 즐겨찾기에 있는지 확인
        if (favoriteDao.existsFavorite(userId, storeId)) {
            throw new BizException("이미 즐겨찾기에 추가된 매장입니다.");
        }

        // 매장이 존재하는지 확인 (선택적)
        // storeDao.selectStoreById(storeId) - 필요에 따라 추가

        FavoriteDto favoriteDto = new FavoriteDto();
        favoriteDto.setUserId(userId);
        favoriteDto.setStoreId(storeId);
        favoriteDto.setCreatedBy(userId);
        favoriteDto.setUpdatedBy(userId);

        int result = favoriteDao.insertFavorite(favoriteDto);
        if (result == 0) {
            throw new BizException("즐겨찾기 추가에 실패했습니다.");
        }

        log.info("즐겨찾기 추가 완료 - userId: {}, storeId: {}", userId, storeId);
    }

    @Override
    @Transactional
    public void removeFavorite(String userId, Long storeId) {
        int result = favoriteDao.deleteFavorite(userId, storeId, userId);
        if (result == 0) {
            throw new BizException("즐겨찾기에서 삭제할 매장을 찾을 수 없습니다.");
        }

        log.info("즐겨찾기 삭제 완료 - userId: {}, storeId: {}", userId, storeId);
    }

    @Override
    public boolean isFavorite(String userId, Long storeId) {
        return favoriteDao.existsFavorite(userId, storeId);
    }

    /**
     * FavoriteDto를 FavoriteResponse로 변환
     */
    private FavoriteResponse convertToResponse(FavoriteDto dto) {
        FavoriteResponse response = new FavoriteResponse();
        response.setId(dto.getId());
        response.setStoreId(dto.getStoreId());
        response.setStoreName(dto.getStoreName());
        response.setStoreDescription(dto.getStoreDescription());
        response.setCategory(dto.getCategory());
        response.setPhone(dto.getPhone());
        response.setAddress(dto.getAddress());
        response.setLatitude(dto.getLatitude());
        response.setLongitude(dto.getLongitude());
        response.setStatus(dto.getStatus());
        response.setImageUrl(dto.getImageUrl());
        response.setMinOrderAmount(dto.getMinOrderAmount());
        response.setDeliveryFee(dto.getDeliveryFee());
        response.setDeliveryTime(dto.getDeliveryTime());
        response.setBusinessNumber(dto.getBusinessNumber());
        response.setIsFeatured(dto.getIsFeatured());
        response.setCreatedAt(dto.getCreatedAt());
        return response;
    }
}