package com.winter.winter_back.domain.home.controller;

import com.winter.winter_back.domain.home.dto.HomeFeedRequest;
import com.winter.winter_back.domain.home.dto.HomeFeedResponse;
import com.winter.winter_back.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description    : 홈 컨트롤러
 * fileName       : HomeCtrl
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 */


@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/feed")
    public HomeFeedResponse getHomeFeed(@ModelAttribute HomeFeedRequest request) {
        return homeService.getHomeFeed(request.getLat(), request.getLng());
    }

}
