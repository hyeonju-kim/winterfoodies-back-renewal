package com.winter.winter_back.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * description    : 매장 상세 응답 객체
 * fileName       : StoreDetailResponse
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDetailResponse {
    private StoreDto store;
    private Double distance; // km
    private List<StoreHoursDto> hours;
    private List<StoreCategoryDto> categories;
}
