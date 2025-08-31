package com.winter.winter_back.common.security.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 사용자 역할 정보
 */
@Data
public class ComRoleUserDto implements Serializable {
	private static final long serialVersionUID = 1L; // serialVersionUID 추가
	private String roleId; // 역할 ID
}
