package com.winter.winter_back.domain.favorite.service;

import com.winter.winter_back.domain.favorite.dto.FavoriteListResponse;

/**
 * description    :
 * fileName       : FavoriteService
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */

public interface FavoriteService {

    /**
     * 즐겨찾기 목록 조회
     * @param userId 사용자 ID
     * @return 즐겨찾기 목록
     */
    FavoriteListResponse getFavoriteList(String userId);

    /**
     * 즐겨찾기 추가
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     */
    void addFavorite(String userId, Long storeId);

    /**
     * 즐겨찾기 삭제
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     */
    void removeFavorite(String userId, Long storeId);

    /**
     * 즐겨찾기 여부 확인
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @return 즐겨찾기 여부
     */
    boolean isFavorite(String userId, Long storeId);
}
