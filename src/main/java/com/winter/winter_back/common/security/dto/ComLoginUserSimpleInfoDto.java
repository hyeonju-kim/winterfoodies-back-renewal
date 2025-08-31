package com.winter.winter_back.common.security.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 사용자의 간단한 정보와 관련된 필드
 */
@Data
public class ComLoginUserSimpleInfoDto implements Serializable {
	private String userName; // 사용자 명
	private String mqttPassword; // MQTT 연결에 사용되는 비밀번호
	private String mqttDeviceId; // MQTT 연결에 사용되는 디바이스 ID
	private List<ComLoginUserMenuDto> menuScreenInfo; // 사용자에 할당된 메뉴 및 화면 목록
}
