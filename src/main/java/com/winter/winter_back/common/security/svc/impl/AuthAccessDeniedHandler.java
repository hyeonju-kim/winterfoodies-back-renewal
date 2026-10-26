package com.winter.winter_back.common.security.svc.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *  권한이 없는 url 접근할 경우 처리 (인가 실패 이후 처리)
 */
@Slf4j
@RequiredArgsConstructor
public class AuthAccessDeniedHandler implements AccessDeniedHandler {
	private final ObjectMapper objectMapper;
	private final ExceptionWrapperUtil exceptionInfoConfig;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ade)
			throws IOException, ServletException {

		log.info("######## 접근 권한 없음 ########");
		log.info("# uri={}", request.getRequestURI()); // 접근 거부된 요청 경로

		// 응답 생성
		ResponseBaseDto comResultDto = exceptionInfoConfig.makeResponse("permission_denied");
		response.setStatus(comResultDto.getStatus());
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().print(objectMapper.writeValueAsString(comResultDto));
		response.getWriter().flush();
	}
}
