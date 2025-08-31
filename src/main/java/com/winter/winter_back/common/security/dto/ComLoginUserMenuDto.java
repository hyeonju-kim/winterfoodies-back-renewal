package com.winter.winter_back.common.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 로그인 사용자에게 할당된 메뉴 정보를 저장
 */
@Data
public class ComLoginUserMenuDto implements Serializable {
	private static final long serialVersionUID = 1L; // serialVersionUID 추가
	private String menuId; // 메뉴 아이디
	private String parentMenuId; // 상위 메뉴 아이디
	private String menuName; // 메뉴 명
	private String menuIndex; // 메뉴 순서 (동일 레벨)
	@JsonIgnore
	private String menuOrder; // 메뉴 순서 (전체 메뉴에서)
	private String menuUrl; // 대표 화면 경로
}
