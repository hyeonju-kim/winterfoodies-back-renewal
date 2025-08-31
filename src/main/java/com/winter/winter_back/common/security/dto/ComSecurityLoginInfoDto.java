package com.winter.winter_back.common.security.dto;

/**
 * @systemName : abacus-common-web-security
 * @programName : ComSecurityLoginInfoDto
 * @author : 이성진
 * @createDate : 2021. 9. 13.
 * @description :
 */
public class ComSecurityLoginInfoDto {
	private String loginPageUrl;
	private String processingUrl;
	private ComSecurityUserInfoDto user;
	private String successUrl;
	private String secretKey;

	public String getProcessingUrl() {
		return processingUrl;
	}

	public void setProcessingUrl(String processingUrl) {
		this.processingUrl = processingUrl;
	}

	public ComSecurityUserInfoDto getUser() {
		return user;
	}

	public void setUser(ComSecurityUserInfoDto user) {
		this.user = user;
	}

	public String getSuccessUrl() {
		return successUrl;
	}

	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	/**
	 * @return the loginPageUrl
	 */
	public String getLoginPageUrl() {
		return loginPageUrl;
	}

	/**
	 * @param loginPageUrl the loginPageUrl to set
	 */
	public void setLoginPageUrl(String loginPageUrl) {
		this.loginPageUrl = loginPageUrl;
	}
}
