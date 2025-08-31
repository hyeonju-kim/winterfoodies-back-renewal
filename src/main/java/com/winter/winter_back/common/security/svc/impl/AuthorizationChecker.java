package com.winter.winter_back.common.security.svc.impl;

import com.winter.winter_back.common.security.dto.ComRoleUserDto;
import com.winter.winter_back.common.security.svc.SecuritySupportSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AuthorizationChecker {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String applicationName;

	private SecuritySupportSvc securitySupportSvc;

	public AuthorizationChecker(SecuritySupportSvc securitySupportSvc, String applicationName) {
		this.securitySupportSvc = securitySupportSvc;
		this.applicationName = applicationName;
	}

	public boolean check(String urlPattern) {
		logger.debug("########## url 접근 권한 체크 START ##########");
		logger.debug("urlPattern => {}", urlPattern);

		Map<String, HashSet<String>> arrangeApi = securitySupportSvc.getRoleApiAuthMapgForSpringSecurity("MGMT");
		HashSet<String> roleList = arrangeApi.get(urlPattern);
		logger.debug("roleList => {}", roleList);

		boolean access = false;
		// 해당 url 및 메소드의 접근권한을 갖은 역할과 로그인사용자가 소유한 역할중 동일한게 있다면, 접근권한 있음
		if (roleList != null) {
			if (SessionAttributeManager.getLoginUserInfo() != null) { // 세션에 저장된 사용자의 Principle 의 Role만 확인 (menu 목록은 확인 x)
				List<ComRoleUserDto> loginUserRoleList = SessionAttributeManager.getLoginUserInfo().getRoleList();
				for (ComRoleUserDto roleUsr : loginUserRoleList) {
					for (String roleId : roleList) {
						if (roleUsr.getRoleId().equals(roleId)) {
							logger.debug("HAS ROLE => {}", roleId);
							access = true;
							break;
						}
					}

					if (access)
						break;
				}
			}
		}

		logger.debug("########## url 접근 권한 체크 END ##########");

		return access;
	}
}
