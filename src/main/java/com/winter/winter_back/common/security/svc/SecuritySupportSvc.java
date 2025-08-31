package com.winter.winter_back.common.security.svc;


import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.common.security.dto.ComLoginUserMenuDto;
import com.winter.winter_back.common.security.dto.ComRoleUserDto;

import java.util.HashSet;
import java.util.List;
import java.util.Map;


public interface SecuritySupportSvc {

	// 1. 사용자의 암호화된 패스워드를 조회
	public String selectUserEncPswd(String userId);

	// 2. 로그인할 사용자 정보를 조회
	public ComLoginUserInfoDto selectUser(String userId);

	// 3. 로그인 요청한 사용자의 역할 정보를 조회
	public List<ComRoleUserDto> getLoginUserRole(String userId);

	// 4. 역할에 할당된 메뉴 및 화면 정보를 조회
	public List<ComLoginUserMenuDto> getRoleMenuScrn(List<ComRoleUserDto> roleList, boolean forLginUsr);

	// 5. 스프링 시큐리티 관련 역할-API-권한 매핑 전체 정보를 조회
	public Map<String, HashSet<String>> getRoleApiAuthMapgForSpringSecurity(String applicationName);

	// 6. 로그인 이력을 저장
	public void insertLoginUser(String userId);

	// 7. 캐시된 역할-API-권한 매핑 정보를 삭제
	public void clearCacheRoleApiAuthMapg();
}
