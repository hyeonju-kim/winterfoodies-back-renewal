package com.winter.winter_back.common.security.constant;

/**
 * 
 * @programName : ComConstantAuth
 * @author : cwcho
 * @description : 권한관리관련 상수
 */
public class ComConstantAuth {

	/* 권한 종류 , 소유자 */
	public static final String AUTH_TYPE_OWN = "OWN";
	/* 권한 종류 , 동일 그룹내 */
	public static final String AUTH_TYPE_GROUP = "GROUP";
	/* 권한 종류 , 하위 그룹 포함 */
	public static final String AUTH_TYPE_SUB_GROUP = "SUB_GROUP";
	/* 권한 종류 , 회사 전체 */
	public static final String AUTH_TYPE_CORP = "CORP";
	/* 권한 종류 , 전체 */
	public static final String AUTH_TYPE_ALL = "ALL";

	/* 최대 권한자 역할 아이디 */
	public static final String ROLE_TOP = "ROLE_SYSTEM";
	/* 기본 사용자 역할(모든 사용자가 기본으로 할당되는 역할) */
	public static final String ROLE_DEFAULT = "ROLE_USER";

	/* CRUD 권한 구분 */
	/**
	 * 읽기
	 */
	public static final String RWD_R = "_R";

	/**
	 * 쓰기
	 */
	public static final String RWD_W = "_W";

	/**
	 * 삭제
	 */
	public static final String RWD_D = "_D";

	public static final String[] defautAuthList = {
			"OWN_W",
			"GROUP_W",
			"SUB_GROUP_W",
			"CORP_W",
			"ALL_W",
			"OWN_R",
			"GROUP_R",
			"SUB_GROUP_R",
			"CORP_R",
			"ALL_R",
			"OWN_D",
			"GROUP_D",
			"SUB_GROUP_D",
			"CORP_D",
			"ALL_D"
	};

}
