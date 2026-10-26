package com.winter.winter_back.domain.user.controller;

import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.user.dto.PasswordChangeRequest;
import com.winter.winter_back.domain.user.dto.UserDto;
import com.winter.winter_back.domain.user.dto.UserProfileUpdateRequest;
import com.winter.winter_back.domain.user.dto.WithdrawRequest;
import com.winter.winter_back.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * description    : 유저 컨트롤러
 * fileName       : UserController
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 * 26. 10. 26.       hj kim             내 정보를 DB 에서 조회(수정 반영), 내 정보 수정·비밀번호 변경·회원탈퇴 추가
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
     * 내 정보 조회
     */
    @GetMapping("/me")
    public UserDto getCurrentUser(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {
        return userService.getMyProfile(loginUserInfo.getUserId());
    }

    /**
     * 내 정보 수정 (이름·전화번호)
     */
    @PutMapping("/me")
    public UserDto modMyProfile(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
            @RequestBody @Valid UserProfileUpdateRequest request) {
        return userService.modMyProfile(loginUserInfo.getUserId(), request);
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/me/password")
    public void modMyPassword(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
            @RequestBody @Valid PasswordChangeRequest request) {
        userService.modMyPassword(loginUserInfo.getUserId(), request);
    }

    /**
     * 회원탈퇴
     */
    @DeleteMapping("/me")
    public void delMyAccount(
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
            @RequestBody @Valid WithdrawRequest request) {
        userService.delMyAccount(loginUserInfo.getUserId(), request);
    }
}
