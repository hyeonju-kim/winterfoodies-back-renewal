package com.winter.winter_back.common.security.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
	private List<ComLoginUserMenuDto> menuScreenInfo; // 사용자에 할당된 메뉴 및 화면 목록
	private List<ComRoleUserDto> roleList; // 사용자에 할당된 역할 목록
	private ComLoginUserSimpleInfoDto lginUserSimpleInfo; // 로그인 사용자 API 응답용 정보
	private String mqttPassword; // MQTT 연결에 사용되는 비밀번호
	private String mqttDeviceId; // MQTT 연결에 사용되는 디바이스 ID
	private Map<String, String> apiAuthPermit; // 사용자가 할당받은 API 권한 정보
}