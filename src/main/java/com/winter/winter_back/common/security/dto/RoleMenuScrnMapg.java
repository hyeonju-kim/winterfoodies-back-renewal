package com.winter.winter_back.common.security.dto;

/**
 * 역할 메뉴 화면 매핑
 */
public class RoleMenuScrnMapg {

	/**
	 * 메뉴 아이디
	 */
	private String menuId;

	/**
	 * 메뉴 명
	 */
	private String menuNm;

	/**
	 * 상위 메뉴 명
	 */
	private String hposMenuId;

	/**
	 * 메뉴 깊이
	 */
	private String lvl;

	/**
	 * 메뉴 경로
	 */
	private String menuPath;

	/**
	 * 메인 화면 여부
	 */
	private String mainScrnYn;

	/**
	 * 메뉴 링크
	 */
	private String menuUrl;

	/**
	 * 메뉴 순서(동일레벨)
	 */
	private String menuIdx;

	/**
	 * 메뉴 순서(전체메뉴에서)
	 */
	private String menuOrder;

	public String getMenuId() {
		return menuId;
	}

	public String getMenuNm() {
		return menuNm;
	}

	public String getHposMenuId() {
		return hposMenuId;
	}

	public String getLvl() {
		return lvl;
	}

	public String getMenuPath() {
		return menuPath;
	}

	public String getMainScrnYn() {
		return mainScrnYn;
	}

	public String getMenuIdx() {
		return menuIdx;
	}

	public void setMenuIdx(String menuIdx) {
		this.menuIdx = menuIdx;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public void setMenuNm(String menuNm) {
		this.menuNm = menuNm;
	}

	public void setHposMenuId(String hposMenuId) {
		this.hposMenuId = hposMenuId;
	}

	public void setLvl(String lvl) {
		this.lvl = lvl;
	}

	public void setMenuPath(String menuPath) {
		this.menuPath = menuPath;
	}

	public void setMainScrnYn(String mainScrnYn) {
		this.mainScrnYn = mainScrnYn;
	}


	public String getMenuUrl() {
		return menuUrl;
	}

	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
	}

	public String getMenuOrder() {
		return menuOrder;
	}

	public void setMenuOrder(String menuOrder) {
		this.menuOrder = menuOrder;
	}
}