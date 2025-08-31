package com.winter.winter_back.common.security.dto;

/**
 * @systemName : abacus-common-web-security
 * @programName : ComSecurityLogoutInfoDto
 * @author : 이성진
 * @createDate : 2021. 9. 13.
 * @description :
 */
public class ComSecurityLogoutInfoDto {
	private String reqUrl;
	private String successUrl;

	public String getReqUrl() {
		return reqUrl;
	}

	public void setReqUrl(String reqUrl) {
		this.reqUrl = reqUrl;
	}

	public String getSuccessUrl() {
		return successUrl;
	}

	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}
}
