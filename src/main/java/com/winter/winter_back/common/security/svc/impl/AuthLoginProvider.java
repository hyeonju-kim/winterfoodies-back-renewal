package com.winter.winter_back.common.security.svc.impl;

import com.winter.winter_back.common.security.constant.ComConstantAuth;
import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.common.security.dto.ComLoginUserMenuDto;
import com.winter.winter_back.common.security.dto.ComRoleUserDto;
import com.winter.winter_back.common.security.svc.SecuritySupportSvc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 인증 처리 클래스
 **
 * 이 클래스는 Spring Security의 AuthenticationProvider를 구현하여 사용자 인증을 처리
 * 사용자가 로그인 시 입력한 아이디와 비밀번호를 검증하고 인증에 성공하면 사용자 정보를 포함한 Authentication 객체를 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthLoginProvider implements AuthenticationProvider { // 제일 처음 로그인 시에만 동작 ! (로그인에 한 번 성공하면, 스프링은 세션에서 SecurityContext를 꺼내 Authentication 정보를 복원하므로)
//	private final PasswordEncoder passwordEncoder;        // 비밀번호 암호화 및 검증을 위한 인코더
	private final SecuritySupportSvc securitySupportSvc;  // 사용자 인증 관련 서비스
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // 직접 생성


	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.info("[AuthLoginProvider.java] === 로그인 START ===");
		String userId = authentication.getName();
		String userPwd = authentication.getCredentials().toString();

		log.info("[AuthLoginProvider.java] authenticate > userId === {}, userPwd === {}", userId, userPwd);

		UsernamePasswordAuthenticationToken authenticationToken = null;

		ComLoginUserInfoDto loginUserInfoDto = null;

		// DB에서 암호화된 비밀번호 조회
		String encPswd = securitySupportSvc.selectUserEncPswd(userId);
		if (encPswd == null) {
			// 아이디가 존재하지 않으면 인증 실패
			return null;
		}


		// 비밀번호 검증
		if (passwordEncoder.matches(userPwd, encPswd)) {
			// 비밀번호가 맞으면 사용자 정보를 조회
			loginUserInfoDto = securitySupportSvc.selectUser(userId);
		}

		if (loginUserInfoDto != null) {
			List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
			List<ComRoleUserDto> roleList = securitySupportSvc.getLoginUserRole(userId);
			loginUserInfoDto.setRoleList(roleList); // 로그인 사용자 객체에 역할 정보를 저장

			// 모든 사용자에게 기본 역할 할당
			grantedAuthorityList.add(new SimpleGrantedAuthority(ComConstantAuth.ROLE_DEFAULT)); // ROLE_USER 기본 할당

			for (ComRoleUserDto role : roleList) {
				// 추가적인 역할 할당
				if (!ComConstantAuth.ROLE_DEFAULT.equals(role.getRoleId())) { // ROLE_USER만 제외하고, 할당
					grantedAuthorityList.add(new SimpleGrantedAuthority(role.getRoleId()));
				}
			}

			// 역할에 기반하여 메뉴 및 화면 매핑 정보 조회
			List<ComLoginUserMenuDto> menuScreenInfo = securitySupportSvc.getRoleMenuScrn(roleList, true); // true 역할 사용여부 체크
			loginUserInfoDto.setMenuScreenInfo(menuScreenInfo);

			authenticationToken = new UsernamePasswordAuthenticationToken(loginUserInfoDto, userPwd, grantedAuthorityList);

			// 로그인 이력 저장
			securitySupportSvc.insertLoginUser(userId);
		}
		return authenticationToken; // Authentication 객체를 리턴하면, Spring Security가 자동으로 SecurityContext에 넣는다.
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
