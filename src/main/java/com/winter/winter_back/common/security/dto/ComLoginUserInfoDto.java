package com.winter.winter_back.common.security.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 사용자의 로그인 정보와 권한을 저장하고 관리하는 데 사용
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class ComLoginUserInfoDto implements Serializable {
	private static final long serialVersionUID = 1L;

	private String userId; // 사용자 아이디
	private String id; // 사용자 로그인 ID
	private String pw; // 사용자 비밀번호
	private String userNm; // 사용자 이름
	private String tel; // 사용자 이메일 또는 전화번호
	private List<ComRoleUserDto> roleList; // 사용자에 할당된 역할 목록
}