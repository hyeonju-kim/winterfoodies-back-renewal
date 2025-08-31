package com.winter.winter_back.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description    : 홈 피드 요청 객체
 * fileName       : HomeFeedRequest
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
public class HomeFeedRequest {
    private Double lat;
    private Double lng;
}
