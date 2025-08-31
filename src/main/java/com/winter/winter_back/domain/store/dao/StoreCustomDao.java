package com.winter.winter_back.domain.store.dao;

import com.winter.winter_back.domain.store.dto.StoreDto;
import com.winter.winter_back.domain.store.dto.StoreSearchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    : 매장 커스텀 DAO
 * fileName       : StoreCustomDao
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 */

@Mapper
public interface StoreCustomDao {

    List<StoreDto> findStoresWithDistance(StoreSearchRequest request);

    long countStoresWithDistance(StoreSearchRequest request);

    List<StoreDto> searchStoresWithDistance(StoreSearchRequest request);

    long countSearchStoresWithDistance(StoreSearchRequest request);

    List<StoreDto> findPopularStores(@Param("lat") Double lat,
                                     @Param("lng") Double lng,
                                     @Param("limit") int limit);

    List<StoreDto> findHighRatedStores(@Param("lat") Double lat,
                                       @Param("lng") Double lng,
                                       @Param("limit") int limit);

    List<StoreDto> findNewStores(@Param("lat") Double lat,
                                 @Param("lng") Double lng,
                                 @Param("limit") int limit);

    List<StoreDto> findDiscountStores(@Param("lat") Double lat,
                                      @Param("lng") Double lng,
                                      @Param("limit") int limit);

    List<StoreDto> findRecommendedStores(@Param("lat") Double lat,
                                         @Param("lng") Double lng,
                                         @Param("limit") int limit);

    List<StoreDto> findNearbyStores(@Param("lat") Double lat,
                                    @Param("lng") Double lng,
                                    @Param("limit") int limit);
}