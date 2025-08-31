package com.winter.winter_back.common.security.svc.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import javax.servlet.http.HttpSession;

/**
 * @systemName : abaus-common-web
 * @programName : AuthDynamicCreateUserHandler
 * @author : 이성진
 * @createDate : 2021. 9. 6.
 * @description :
 */
public class AuthDynamicCreateUserHandler {
	private AuthenticationManager authenticationManager;

	public AuthDynamicCreateUserHandler(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public Authentication makeDynamicUser(String id, String pass, Object details) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(id, pass);
		token.setDetails(details);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);

		HttpSession session = SessionAttributeManager.getRequest().getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
		return authentication;
	}
}
