package com.winter.winter_back.common.security.svc.impl;

import com.winter.winter_back.common.security.constant.ComConstantAuth;
import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 세션 정보 관리
 *
 * @programName : SessionAttributeManager
 * @author : 이성진
 * @description :
 */
public class SessionAttributeManager {
	private final static String STR_SEPARATOR = ";";

	/**
	 * 로그인 사용자 정보
	 *
	 * @methodName : getlginUser
	 * @author : 이성진
	 * @return : LginUser
	 * @description :
	 */
	public static ComLoginUserInfoDto getLoginUserInfo() { // 로그인 성공 후, HTTP의 모든 요청을 할 때마다 사용자 정보를 불러옴
		Object userInfo = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ComLoginUserInfoDto lginUser = null;

		if (userInfo instanceof ComLoginUserInfoDto) {
			lginUser = (ComLoginUserInfoDto) userInfo;
		}
		return lginUser;
	}


	public static HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

	public static String getRequestApiAuthType() {
		HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		// 해당 API에 허용된 최상위 권한을 조회한다.
		// return example : OWN 또는 GROUP 또는 CORP 또는 ALL
		String authType = null;
		if (httpServletRequest.getMethod().equals(HttpMethod.GET.toString())) {
			authType = ComConstantAuth.RWD_R;
		} else if (httpServletRequest.getMethod().equals(HttpMethod.PUT.toString())
				|| httpServletRequest.getMethod().equals(HttpMethod.POST.toString())) {
			authType = ComConstantAuth.RWD_W;
		} else if (httpServletRequest.getMethod().equals(HttpMethod.DELETE.toString())) {
			authType = ComConstantAuth.RWD_D;
		}

		// 요청된 URL이 어떤 URL MAPPING 패턴과 일치하는지 조회
		final String pattern = (String) httpServletRequest
				.getAttribute(org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		return getLoginUserInfo().getApiAuthPermit().get(authType + STR_SEPARATOR + pattern);
	}

	/**
	 *
	 * @methodName : checkAuth
	 * @author : cwcho
	 * @param permitType
	 * @return
	 * @return : boolean
	 * @description : 현재 요청된 api의 url 및 메소드 기준으로 권한을 체크한다. 그 권한으로 쿼리의 데이터 범위를 제한한다.
	 */
	public static boolean checkAuth(String permitType) {
		HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String authType = null;
		if (httpServletRequest.getMethod().equals(HttpMethod.GET.toString())) {
			authType = ComConstantAuth.RWD_R;
		} else if (httpServletRequest.getMethod().equals(HttpMethod.PUT.toString())
				|| httpServletRequest.getMethod().equals(HttpMethod.POST.toString())) {
			authType = ComConstantAuth.RWD_W;
		} else if (httpServletRequest.getMethod().equals(HttpMethod.DELETE.toString())) {
			authType = ComConstantAuth.RWD_D;
		}

		// 요청된 URL이 어떤 URL MAPPING 패턴과 일치하는지 조회
		final String pattern = (String) httpServletRequest
				.getAttribute(org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

		// 해당 API에 허용된 최상위 권한을 조회한다.
		// return example : OWN 또는 GROUP 또는 SUB_GROUP 또는 CORP 또는 ALL
		return permitType.equals(getLoginUserInfo().getApiAuthPermit().get(authType + STR_SEPARATOR + pattern));
	}
}
