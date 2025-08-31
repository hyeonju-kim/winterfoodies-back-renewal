package com.winter.winter_back.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description    : 매장 카테고리 DTO
 * fileName       : StoreCategoryDto
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreCategoryDto {
    private Long id;
    private Long storeId;
    private String categoryName;
    private String categoryCode;
}
