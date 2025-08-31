package com.winter.winter_back.common.security.svc.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.winter_back.common.dto.BaseConstant;
import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.common.security.dto.ComLoginUserSimpleInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 *  인증 성공 시 처리
 */

@Slf4j
@RequiredArgsConstructor
public class AuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final ObjectMapper objectMapper;
	private final Environment env;
	private final ExceptionWrapperUtil exceptionWrapperUtil;


	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
										HttpServletResponse response,
										Authentication authentication) throws ServletException, IOException {
		// 인증된 사용자 정보를 가져옴
		ComLoginUserInfoDto lginUsr = (ComLoginUserInfoDto) authentication.getPrincipal();

		// Redis에 username 따로 저장
		request.getSession().setAttribute("username", lginUsr.getUserId());

		// 사용자 정보를 단순화한 객체 생성
		ComLoginUserSimpleInfoDto lginUsrResDto = lginUsr.getLginUserSimpleInfo();


		// 성공 응답 정보를 가져옴
		Map<String, Object> expInfo = exceptionWrapperUtil.getExceptionInfo("success");

		// 성공 응답 DTO 생성
		ResponseBaseDto<ComLoginUserSimpleInfoDto> comResponseDto = new ResponseBaseDto<>(
				String.valueOf(expInfo.get(BaseConstant.RES_CODE)),
				String.valueOf(expInfo.get(BaseConstant.RES_DESC)),
				Integer.valueOf(String.valueOf(expInfo.get(BaseConstant.RES_STATUS))),
				lginUsrResDto);

		// 응답을 JSON 형식으로 설정하고 반환
		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().print(objectMapper.writeValueAsString(comResponseDto));
		response.getWriter().flush();
	}
}
