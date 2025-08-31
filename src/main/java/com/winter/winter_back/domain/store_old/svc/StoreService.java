package com.winter.winter_back.domain.store_old.svc;

import com.winter.winter_back.domain.store_old.dto.CategoryDto;
import com.winter.winter_back.domain.product_old.dto.ProductDto;
import com.winter.winter_back.domain.store_old.dao.StoreDao;
import com.winter.winter_back.domain.store_old.dto.StoreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description    :
 * fileName       : StoreService
 * author         : hyeonju
 * date           : 25. 8. 25.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 25.        hyeonju             최초 생성
 */
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreDao storeDao;

    public List<CategoryDto> getCategories() {
        return storeDao.getCategories();
    }

    public List<StoreDto> getStoresByCategory(Long categoryId) {
        return storeDao.getStoresByCategory(categoryId);
    }

    public List<ProductDto> getProductsByStore(Long storeId) {
        return storeDao.getProductsByStore(storeId);
    }
}
