package com.winter.winter_back.common.security.dto;

import java.util.Objects;

public class ComApiAuthPatternDto {
	/**
	 * url 패턴
	 */
	private String apiPathPtrn;

	/**
	 * 권한(_R, _W, _D)
	 */
	private String authRwd;

	public String getApiPathPtrn() {
		return apiPathPtrn;
	}

	public void setApiPathPtrn(String apiPathPtrn) {
		this.apiPathPtrn = apiPathPtrn;
	}

	public String getAuthRwd() {
		return authRwd;
	}

	public void setAuthRwd(String authRwd) {
		this.authRwd = authRwd;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return Objects.hash(authRwd, apiPathPtrn);
	}

	@Override
	public boolean equals(Object object) {
		ComApiAuthPatternDto apiAuthPattern = (ComApiAuthPatternDto) object;
		return this == object || this.authRwd.equals(apiAuthPattern.getAuthRwd()) && this.apiPathPtrn.equals(apiAuthPattern.getApiPathPtrn());
	}

}
