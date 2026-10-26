package com.winter.winter_back.common.security.config;

import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final ObjectMapper objectMapper;

    // 토큰 갱신 요청은 만료된 access 토큰을 헤더에 실어 오므로 필터 검증을 건너뛴다 (검증은 AuthService 에서 refresh 토큰으로)
    private static final String REFRESH_TOKEN_PATH = "/auth/refresh";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return REFRESH_TOKEN_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = TokenProvider.getAccessTokenFromHeader(request);
        log.info("[doFilterInternal] > [request] === {}", request);
        log.info("[doFilterInternal] > [token] === {}", token);

        if (StringUtils.hasText(token)) {
            int valid = TokenProvider.validToken(token);
            log.info("[doFilterInternal] > [valid] === {}", valid);

            String errorType = null;
            switch (valid) {
                case 0:
                    // 정상 토큰 - 인증 정보 설정
                    SecurityContextHolder.getContext()
                            .setAuthentication(TokenProvider.getAuthentication(token));
                    break;
                case -1:
                    // TokenProvider 초기화 안됨
                    errorType = "token_not_build";
                    break;
                case 2:
                    // 만료된 토큰 - 클라이언트가 /auth/refresh 로 갱신해야 함
                    errorType = "token_expired";
                    break;
                case 5:
                    // 블랙리스트에 있는 토큰 (로그아웃된 토큰)
                    log.warn("[TokenFilter] 블랙리스트에 있는 토큰입니다");
                    errorType = "token_blacklisted";
                    break;
                default:
                    // 잘못된 서명(1) / 지원하지 않는 토큰(3) / 잘못된 형식(4) / 알 수 없는 오류
                    errorType = "token_wrong";
                    break;
            }

            // 필터 단계의 오류는 @RestControllerAdvice 가 처리하지 못하므로 에러 응답을 여기서 직접 쓴다
            if (errorType != null) {
                writeErrorResponse(response, errorType);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, String errorType) throws IOException {
        ResponseBaseDto responseBaseDto = exceptionWrapperUtil.makeResponse(errorType);
        response.setStatus(responseBaseDto.getStatus());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().print(objectMapper.writeValueAsString(responseBaseDto));
        response.getWriter().flush();
    }
}
