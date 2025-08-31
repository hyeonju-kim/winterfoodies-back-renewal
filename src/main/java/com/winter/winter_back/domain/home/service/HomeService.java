package com.winter.winter_back.domain.home.service;

import com.winter.winter_back.domain.home.dto.HomeFeedResponse;

/**
 * description    : 홈 서비스 인터페이스
 * fileName       : HomeService
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 */
public interface HomeService {
    HomeFeedResponse getHomeFeed(Double lat, Double lng);

}
