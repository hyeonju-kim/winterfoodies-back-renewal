package com.winter.winter_back.common.security.svc.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *  로그아웃 성공 시 처리
 */
@Slf4j
@RequiredArgsConstructor
public class AuthLogoutHandler implements LogoutSuccessHandler {
	private final String logoutSuccessUrl;
	@Autowired
	private ExceptionWrapperUtil exceptionInfoConfig;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException {
		// 인증 세션이 있을 경우 세션을 무효화
		if (authentication != null && authentication.getDetails() != null) {
			try {
				// 세션 만료
				request.getSession().invalidate();
				log.info("##### 로그아웃 성공 ######");

			} catch (Exception e) {
				log.error("Error during logout: {}", e.getMessage(), e);
			}
		}
		// 로그아웃 성공 상태 설정
		response.setStatus(HttpServletResponse.SC_OK);

		// 응답 값 정의
		ResponseBaseDto comResultDto = exceptionInfoConfig.makeResponse("success");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");

		response.getWriter().write(new ObjectMapper().writeValueAsString(comResultDto));
		response.getWriter().flush();

		// 로그아웃 성공 후 이동할 URL이 지정된 경우, 리다이렉션 처리
		if (!StringUtils.isEmpty(this.logoutSuccessUrl)) {
			response.sendRedirect(this.logoutSuccessUrl);
		}
	}
}
