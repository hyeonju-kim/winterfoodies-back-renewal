package com.winter.winter_back.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description    : 매장 객체 요청 객체
 * fileName       : StoreSearchRequest
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
public class StoreSearchRequest {
    private String keyword;
    private String category;
    @Builder.Default
    private String sort = "distance"; // distance, rating, popular, name
    private Double lat;
    private Double lng;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 20;
    private Double maxDistance; // km 단위

    // offset 계산 메서드 추가
    public int getOffset() {
        return page * size;
    }
}
