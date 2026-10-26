package com.winter.winter_back.common.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  보안 관련 설정 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComSecurityInfoDto {
	private String[] staticContentsUrlPatterns; // 정적 콘텐츠에 대한 URL 패턴 배열
	private String[] permitAllUrlPatterns; // 모든 사용자에게 허용되는 URL 패턴 배열
}