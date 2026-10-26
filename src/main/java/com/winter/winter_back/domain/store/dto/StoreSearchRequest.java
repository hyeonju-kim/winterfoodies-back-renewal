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
 * 26. 10. 26.       hj kim             검색어 LIKE 이스케이프(getKeywordLike) 추가
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSearchRequest {
    private String keyword;
    private String category;
    @Builder.Default
    private String sort = "distance"; // distance(거리순), rating(평점순), order(주문많은순), review(리뷰순)
    private Double lat;
    private Double lng;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 20;
    private Double maxDistance; // km 단위

    /**
     * LIKE 검색용 키워드: 앞뒤 공백을 빼고, LIKE 특수문자(% _)와 이스케이프 문자(!)를 ! 로 이스케이프.
     * 비어 있으면 null (검색 조건 없음)
     */
    public String getKeywordLike() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    // offset 계산 메서드 추가
    public int getOffset() {
        return page * size;
    }
}
