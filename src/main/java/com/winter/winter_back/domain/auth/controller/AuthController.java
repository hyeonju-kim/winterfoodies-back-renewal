package com.winter.winter_back.domain.auth.controller;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.common.security.config.TokenProvider;
import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.auth.dto.LoginRequest;
import com.winter.winter_back.domain.auth.dto.RefreshTokenRequest;
import com.winter.winter_back.domain.auth.dto.TokenVo;
import com.winter.winter_back.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * description    : 유저 권한 컨트롤러
 * fileName       : AuthController
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenVo login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }


    /**
     * 클라이언트에서 호출해야 하는 API
     * @param request
     * @param refreshRequest
     * @return
     */
    @PostMapping("/auth/refresh")
    public TokenVo refreshToken(HttpServletRequest request,
                                @RequestBody RefreshTokenRequest refreshRequest) {
        log.debug("[refreshToken] > [request] === {}", request);
        log.debug("[refreshToken] > [refreshRequest] === {}", refreshRequest);

        return authService.refreshToken(request, refreshRequest);
    }

    /**
     * 로그아웃
     * @param loginUserInfo
     * @param request
     */
    @PostMapping("/auth/logout")
    public void logout(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo, HttpServletRequest request) {
        try {
            String token = TokenProvider.getAccessTokenFromHeader(request);
            log.debug("[logout] token: {}", token);

            if (token != null && loginUserInfo != null) {
                authService.logout(loginUserInfo.getUserId());
            } else {
                log.error("[AuthController.java] logout data가 null입니다.");

            }
        } catch (Exception e) {
            log.error("[logout] 로그아웃 실패: {}", e.getMessage());
            throw new BizException("logout_fail");
        }
    }
}