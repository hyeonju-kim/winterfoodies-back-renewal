package com.winter.winter_back.domain.store_old.dao;

import com.winter.winter_back.domain.store_old.dto.CategoryDto;
import com.winter.winter_back.domain.product_old.dto.ProductDto;
import com.winter.winter_back.domain.store_old.dto.StoreDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * description    :
 * fileName       : StoreDao
 * author         : hyeonju
 * date           : 25. 8. 25.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 25.        hyeonju             최초 생성
 */
@Mapper
public interface StoreDao {
    List<CategoryDto> getCategories();
    List<StoreDto> getStoresByCategory(Long categoryId);
    List<ProductDto> getProductsByStore(Long storeId);
}
