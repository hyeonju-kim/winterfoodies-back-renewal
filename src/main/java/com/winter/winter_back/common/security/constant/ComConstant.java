package com.winter.winter_back.common.security.constant;

/**
 *
 * @programName : ComConstant
 * @author : cwcho
 * @description : 공통 상수
 */
public class ComConstant {

	/**
	 * 사용자 그룹 기본 아이디
	 */
	public final static String DEFAULT_USR_GROUP_ID = "DEFAULT";

	/**
	 * 페이징 처리시, 페이지 요청이 빈값일 경우 기본값
	 */
	public final static int DEFAULT_PAGE = 1;

	/*
	 * 페이징 처리시, 페이지당 조회수량이 빈값일 경우 기본값
	 */
	public final static int DEFAULT_LIMIT = 20;

	/**
	 * YN 문자열 Y
	 */
	public final static String YN_Y = "Y";

	/**
	 * YN 문자열 N
	 */
	public final static String YN_N = "N";

	/**
	 * 처리구분 생성(C)
	 */
	public final static String CUD_C = "C";

	/**
	 * 처리구분 생성(U)
	 */
	public final static String CUD_U = "U";

	/**
	 * 처리구분 생성(D)
	 */
	public final static String CUD_D = "D";

	/**
	 * 정기/주기 구분 코드
	 * 정기
	 */
	public final static String CYCLE_CODE_RT = "RT";

	/**
	 * 정기/주기 구분 코드
	 * 주기
	 */
	public final static String CYCLE_CODE_CY = "CY";

	/**
	 * 로봇 접속
	 */
	public final static String ROBOT_CONNECTED = "CONNECTED";
	/**
	 * 로봇 접속 끊김
	 */
	public final static String ROBOT_DISCONNECTED = "DISCONNECTED";

	// response key
	public final static String RES_CODE = "code";
	public final static String RES_DESC = "desc";
	public final static String RES_STATUS = "status";
	public final static String RES_SUCCESS_DATA = "response success";

	// common string words
	public final static String YES = "Y";
	public final static String NO = "N";
	public final static String STRING_BLANK = " ";
	public final static String STRING_HYPHEN = "-";
	public final static String STRING_COMMA = ",";
	public final static String STRING_EMPTY = "";
	public final static String STRING_PERIOD = ".";

	// encode type
	public final static String ENCODE_BASE64 = "base64";

	// date pattern
	public final static String DATE_PATTERN = "yyyy-MM-dd";
	public final static String TIME_PATTERN = "HH:mm:ss";
	public final static String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public final static String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public final static String TIMEZONE_UTC = "UTC";
	public final static String TIMEZONE_KR = "Asia/Seoul";
}
