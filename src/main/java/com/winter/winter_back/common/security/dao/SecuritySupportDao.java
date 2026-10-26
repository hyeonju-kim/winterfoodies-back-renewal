package com.winter.winter_back.common.security.dao;

import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.common.security.dto.ComRoleUserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface SecuritySupportDao {
	/**
	 * @description : 사용자의 암호화된 패스워드 조회
	 */
	String selectUserEncPswd(String userId);

	/**
	 * @description : 로그인 할 사용자 정보 조회
	 */
	ComLoginUserInfoDto selectUser(String userId);

	/**
	 * @description : 사용자에 할당된 역할 조회
	 */
	List<ComRoleUserDto> getLoginUserRole(String userId);

	/**
	 * @description : 사용자 로그인 정보 저장
	 */
	Integer insertLoginUser(String userId);
}
