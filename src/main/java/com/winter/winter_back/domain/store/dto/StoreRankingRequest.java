package com.winter.winter_back.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description    : 매장 랭킹 요청 객체
 * fileName       : StoreRankingRequest
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
public class StoreRankingRequest {
    private String type;
    private Double lat;
    private Double lng;
    @Builder.Default
    private int limit = 10;
}
