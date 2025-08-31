package com.winter.winter_back.domain.home.dto;

import com.winter.winter_back.domain.store.dto.StoreDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * description    : 홈 피드 응답 객체
 * fileName       : HomeFeedResponse
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
public class HomeFeedResponse {
    private List<StoreDto> recommendedStores;
    private List<StoreDto> popularStores;
    private List<StoreDto> nearbyStores;
    private List<StoreDto> newStores;
}