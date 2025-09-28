package com.winter.winter_back.common.security.config;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * description    : 토큰 필터 클래스
 * fileName       : TokenFilter
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 * 25. 9. 28.         hj kim             블랙리스트 토큰 처리 추가
 */
@RequiredArgsConstructor
@Slf4j
public class TokenFilter extends OncePerRequestFilter {

    private final ExceptionWrapperUtil exceptionWrapperUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = TokenProvider.getAccessTokenFromHeader(request);
        log.info("[doFilterInternal] > [request] === {}", request);
        log.info("[doFilterInternal] > [token] === {}", token);

        if (StringUtils.hasText(token)) {
            int valid = TokenProvider.validToken(token);
            log.info("[doFilterInternal] > [valid] === {}", valid);

            switch (valid) {
                case 0:
                    // 정상 토큰 - 인증 정보 설정
                    SecurityContextHolder.getContext()
                            .setAuthentication(TokenProvider.getAuthentication(token));
                    break;
                case -1:
                    // TokenProvider 초기화 안됨
                    throw new BizException("token_not_build");
                case 1:
                    // 잘못된 서명
                    throw new BizException("token_wrong");
                case 2:
                    // 만료된 토큰
                    throw new BizException("token_expire");
                case 3:
                    // 지원하지 않는 토큰
                    throw new BizException("token_wrong");
                case 4:
                    // 잘못된 토큰 형식
                    throw new BizException("token_wrong");
                case 5:
                    // 블랙리스트에 있는 토큰 (로그아웃된 토큰)
                    log.warn("[TokenFilter] 블랙리스트에 있는 토큰입니다");
                    throw new BizException("token_blacklisted");
                default:
                    // 알 수 없는 오류
                    throw new BizException("token_wrong");
            }
        }

        filterChain.doFilter(request, response);
    }
}