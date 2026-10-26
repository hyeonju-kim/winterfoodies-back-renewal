package com.winter.winter_back.domain.store.dao;

import com.winter.winter_back.domain.menu.dto.MenuCategoryResponse;
import com.winter.winter_back.domain.store.dto.StoreCategoryDto;
import com.winter.winter_back.domain.store.dto.StoreDto;
import com.winter.winter_back.domain.store.dto.StoreHoursDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    : 매장 DAO
 * fileName       : StoreDao
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 * 26. 10. 26.       hj kim             가게 통계 다시 계산 추가
 */

@Mapper
public interface StoreDao {

    StoreDto findById(Long storeId);

    List<StoreDto> findAll();

    List<StoreHoursDto> findStoreHours(Long storeId);

    List<StoreCategoryDto> findStoreCategories(Long storeId);

    List<MenuCategoryResponse> findMenusByStore(Long storeId);

    StoreDto findByIdWithRating(Long storeId);

    int mergeStoreStats(@Param("storeId") Long storeId); // 가게 통계(평균 별점·리뷰 수·받아간 주문 수) 다시 계산

}