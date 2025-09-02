package com.winter.winter_back.domain.favorite.dao;

import com.winter.winter_back.domain.favorite.dto.FavoriteDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    :
 * fileName       : FavoriteDao
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */


@Mapper
public interface FavoriteDao {

    /**
     * 사용자의 즐겨찾기 목록 조회 (매장 정보 포함)
     * @param userId 사용자 ID (varchar)
     * @return 즐겨찾기 목록
     */
    List<FavoriteDto> selectFavoriteListByUserId(@Param("userId") String userId);

    /**
     * 즐겨찾기 추가
     * @param favoriteDto 즐겨찾기 정보
     * @return 추가된 행 수
     */
    int insertFavorite(FavoriteDto favoriteDto);

    /**
     * 즐겨찾기 삭제 (논리 삭제)
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @param updatedBy 수정자
     * @return 삭제된 행 수
     */
    int deleteFavorite(@Param("userId") String userId, @Param("storeId") Long storeId, @Param("updatedBy") String updatedBy);

    /**
     * 즐겨찾기 물리 삭제 (필요 시 사용)
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @return 삭제된 행 수
     */
    int deleteFavoritePhysical(@Param("userId") String userId, @Param("storeId") Long storeId);

    /**
     * 즐겨찾기 여부 확인
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @return 즐겨찾기 여부
     */
    boolean existsFavorite(@Param("userId") String userId, @Param("storeId") Long storeId);

    /**
     * 특정 즐겨찾기 조회
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @return 즐겨찾기 정보
     */
    FavoriteDto selectFavoriteByUserIdAndStoreId(@Param("userId") String userId, @Param("storeId") Long storeId);

    /**
     * 사용자의 전체 즐겨찾기 삭제 (논리 삭제)
     * @param userId 사용자 ID
     * @param updatedBy 수정자
     * @return 삭제된 행 수
     */
    int deleteAllFavoritesByUserId(@Param("userId") String userId, @Param("updatedBy") String updatedBy);

    /**
     * 매장의 전체 즐겨찾기 삭제 (논리 삭제)
     * @param storeId 매장 ID
     * @param updatedBy 수정자
     * @return 삭제된 행 수
     */
    int deleteAllFavoritesByStoreId(@Param("storeId") Long storeId, @Param("updatedBy") String updatedBy);

    /**
     * 즐겨찾기 복구
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @param updatedBy 수정자
     * @return 복구된 행 수
     */
    int restoreFavorite(@Param("userId") String userId, @Param("storeId") Long storeId, @Param("updatedBy") String updatedBy);
}