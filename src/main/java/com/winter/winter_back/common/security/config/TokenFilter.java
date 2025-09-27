package com.winter.winter_back.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.winter_back.common.dto.ResponseBaseDto;
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

        try {
            if (StringUtils.hasText(token)) {
                int valid = TokenProvider.validToken(token);
                log.info("[doFilterInternal] > [valid] === {}", valid);

                switch (valid) {
                    case 0:
                        SecurityContextHolder.getContext()
                                .setAuthentication(TokenProvider.getAuthentication(token));
                        break;
                    case -1:
                        throw new BizException("token_not_build");
                    case 2:
                        throw new BizException("token_expire");
                    case 1:
                    case 3:
                    case 4:
                    default:
                        throw new BizException("token_wrong");
                }
            }

            filterChain.doFilter(request, response);

        } catch (BizException e) {
            setErrorResponse(response, exceptionWrapperUtil.makeResponse(e.getType(), e.getOptions()));
        } catch (Exception e) {
            log.error("TokenFilter error: ", e);
            setErrorResponse(response,
                    exceptionWrapperUtil.makeResponse("INTERNAL_ERROR"));
        }
    }

    private void setErrorResponse(HttpServletResponse response, ResponseBaseDto responseDto)
            throws IOException {
        response.setStatus(responseDto.getStatus());
        response.setContentType("application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(responseDto));
    }
}
