package com.winter.winter_back.domain.store_old.ctrl;

import com.winter.winter_back.domain.store_old.dto.CategoryDto;
import com.winter.winter_back.domain.product_old.dto.ProductDto;
import com.winter.winter_back.domain.store_old.dto.StoreDto;
import com.winter.winter_back.domain.store_old.svc.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * description    :
 * fileName       : StoreController
 * author         : hyeonju
 * date           : 25. 8. 25.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 25.        hyeonju             최초 생성
 */
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /** 카테고리 전체 조회 */
    @GetMapping("/categories")
    public List<CategoryDto> getCategories() {
        return storeService.getCategories();
    }

    /** 카테고리별 상점 조회 */
    @GetMapping("/category/{categoryId}")
    public List<StoreDto> getStoresByCategory(@PathVariable Long categoryId) {
        return storeService.getStoresByCategory(categoryId);
    }

    /** 상점별 메뉴 조회 */
    @GetMapping("/{storeId}/products")
    public List<ProductDto> getProductssByStore(@PathVariable Long storeId) {
        return storeService.getProductsByStore(storeId);
    }
}
