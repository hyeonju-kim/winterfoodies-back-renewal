package com.winter.winter_back.domain.user.controller;

import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.user.dto.UserDto;
import com.winter.winter_back.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * description    : 유저 컨트롤러
 * fileName       : UserController
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/register")
    public void register(@RequestBody @Valid UserDto userDto) {
        userService.register(userDto);
    }


    /**
     * 내 정보 조회 (시큐리티컨텍스트에서 조회) -> 토큰 형식으로 변경함
     */
    @GetMapping("/me")
    public UserDto getCurrentUser(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {
        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        log.info("[getCurrentUser] > [userId] === {}", loginUserInfo.getUserId());
        log.info("[getCurrentUser] > [userNm] === {}", loginUserInfo.getUserNm());
        log.info("[getCurrentUser] > [tel] === {}", loginUserInfo.getTel());

        // 이미 모든 사용자 정보가 들어있으니 DB 조회 없이 바로 변환
        UserDto userDto = new UserDto();
        userDto.setId(loginUserInfo.getUserId());
        userDto.setName(loginUserInfo.getUserNm());
        userDto.setTel(loginUserInfo.getTel());
        return userDto;
    }



}
