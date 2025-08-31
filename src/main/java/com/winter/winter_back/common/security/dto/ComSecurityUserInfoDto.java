package com.winter.winter_back.common.security.dto;

/**
 * @systemName : abacus-common-web-security
 * @programName : ComSecurityUserInfoDto
 * @author : 이성진
 * @createDate : 2021. 9. 13.
 * @description :
 */
public class ComSecurityUserInfoDto {
	private String idParamName;
	private String pwdParamName;

	public String getIdParamName() {
		return idParamName;
	}

	public void setIdParamName(String idParamName) {
		this.idParamName = idParamName;
	}

	public String getPwdParamName() {
		return pwdParamName;
	}

	public void setPwdParamName(String pwdParamName) {
		this.pwdParamName = pwdParamName;
	}
}
