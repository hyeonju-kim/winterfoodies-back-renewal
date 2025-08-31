package com.winter.winter_back.common.security.svc.impl;

import com.winter.winter_back.common.security.dao.SecuritySupportDao;
import com.winter.winter_back.common.security.dto.*;
import com.winter.winter_back.common.security.svc.SecuritySupportSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SecuritySupportSvcImpl implements SecuritySupportSvc {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private SecuritySupportDao securitySupportDao;

	@Value("${spring.application.name}")
	private String appName;

	@Override
	public List<ComRoleUserDto> getLoginUserRole(String userId) {
		return securitySupportDao.getLoginUserRole(userId);
	}

//	@Cacheable(value = "cacheNameTest") //  추가
//	@Override
//	public List<ComRoleUserDto> getLoginUserRole(String userId) {
//		return securitySupportDao.getLoginUserRole(userId);
//	}

	@Override
	public List<ComLoginUserMenuDto> getRoleMenuScrn(List<ComRoleUserDto> roleList, boolean checkUseYn) {
		// 역할별 메뉴 트리를 정리한다.

		List<RoleMenuScrnMapg> roleMenuScrnMapgList = null;
		if (roleList != null && !roleList.isEmpty()) {
			if (checkUseYn) {
				// 역할 및 메뉴의 사용여부를 체크하여 조회
				roleMenuScrnMapgList = securitySupportDao.getRoleMenuScrnRoleUse(roleList);
			} else {
				roleMenuScrnMapgList = securitySupportDao.getRoleMenuScrn(roleList);
			}
		}

		Map<String, ComLoginUserMenuDto> menuMap = new HashMap<>();

		// 메뉴 순서 정렬
		List<ComLoginUserMenuDto> lginUsrMenuDtoList = new ArrayList<>();

		// List to Map(메뉴ID기준) 정렬
		if (roleMenuScrnMapgList != null) {
			for (RoleMenuScrnMapg roleMenuScrnMapg : roleMenuScrnMapgList) {
				if (menuMap.get(roleMenuScrnMapg.getMenuId()) == null) {
					ComLoginUserMenuDto lginUsrMenuDto = new ComLoginUserMenuDto();
					lginUsrMenuDto.setMenuId(roleMenuScrnMapg.getMenuId());
					lginUsrMenuDto.setMenuIndex(roleMenuScrnMapg.getMenuIdx());
					lginUsrMenuDto.setMenuName(roleMenuScrnMapg.getMenuNm());
					lginUsrMenuDto.setParentMenuId(roleMenuScrnMapg.getHposMenuId());
					lginUsrMenuDto.setMenuOrder(roleMenuScrnMapg.getMenuOrder());
					lginUsrMenuDto.setMenuUrl(roleMenuScrnMapg.getMenuUrl());

					menuMap.put(roleMenuScrnMapg.getMenuId(), lginUsrMenuDto);
				}
			}

			// 1. Map to List
			lginUsrMenuDtoList = new ArrayList<>(menuMap.values());

			// 2. 메뉴 순서 정렬
			if (lginUsrMenuDtoList != null && !lginUsrMenuDtoList.isEmpty()) {
				Collections.sort(lginUsrMenuDtoList, new Comparator<ComLoginUserMenuDto>() {
					@Override
					public int compare(ComLoginUserMenuDto a1, ComLoginUserMenuDto a2) {
						if (a1.getMenuIndex().compareTo(a2.getMenuIndex()) >= 0) {
							return 1;
						} else if (a1.getMenuIndex().compareTo(a2.getMenuIndex()) < 0) {
							return -1;
						}

						return 0;
					}
				});
			}
		}

		return lginUsrMenuDtoList;
	}

	@Override
	@Cacheable(value = "roleApiAuthMapgForSpringSecurity", key = "#applicationName") // tb_api 와 tb_auth_api_map 을 조인한 결과를 레디스 캐시에 저장 (사용자의 역할별로 어떤 api에 접근 가능한지를 요청 시마다 매번 확인하기 위해)
	public Map<String, HashSet<String>> getRoleApiAuthMapgForSpringSecurity(String applicationName) {
		List<RoleApiAuthMapg> roleApiAuthMapgList = securitySupportDao.getRoleApiAuthMapgForSpringSecurity();

		// AUTH_ID+API_PATTERN 기준으로 역할목록을 정리한다.
		HashMap<String, HashSet<String>> arrangeApi = new HashMap<>();
		for (RoleApiAuthMapg roleApiAuthMapg : roleApiAuthMapgList) {
			if (arrangeApi.get(roleApiAuthMapg) != null) {
				arrangeApi.get(roleApiAuthMapg.getApiId()).add(roleApiAuthMapg.getRoleId());
			} else {
				HashSet<String> roleSets = new HashSet<String>();
				roleSets.add(roleApiAuthMapg.getRoleId());
				arrangeApi.put(roleApiAuthMapg.getApiPathPtrn(), roleSets);
			}
		}

		return arrangeApi;
	}

	@Override
	@Scheduled(fixedRateString = "${spring.security.reload-role-fixed-rate-string}")
	@CacheEvict(value = "roleApiAuthMapgForSpringSecurity", allEntries = true)
	public void clearCacheRoleApiAuthMapg() {
		logger.debug("# clear cache => roleApiAuthMapgForSpringSecurity #");
	}

	@Override
	public String selectUserEncPswd(String userId) {
		return securitySupportDao.selectUserEncPswd(userId);
	}

	@Override
	public ComLoginUserInfoDto selectUser(String userId) {
		ComLoginUserInfoDto comLoginUserInfoDto = securitySupportDao.selectUser(userId);
		return comLoginUserInfoDto;
	}


	@Override
	public void insertLoginUser(String userId) {
		securitySupportDao.insertLoginUser(userId);
	}
}
