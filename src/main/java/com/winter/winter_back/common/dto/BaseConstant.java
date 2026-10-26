package com.winter.winter_back.common.dto;

/**
 * BaseConstant
 * - 기본 상수 모음
 *
 *  * @author kkb
 *  * @created 2023-11-14
 */
public class BaseConstant {
    public final static String NO = "N";
    // response key
    public final static String RES_CODE = "code";
    public final static String RES_DESC = "desc";
    public final static String RES_STATUS = "status";
    public final static String RES_SUCCESS_DATA = "response success";

    // common string words
    public final static String YES = "Y";
    public final static String STRING_BLANK = " ";
    public final static String STRING_HYPHEN = "-";
    public final static String STRING_COMMA = ",";
    public final static String STRING_EMPTY = "";
    public final static String STRING_PERIOD = ".";
    public final static String STRING_UNDERSCORE = "_";
    public final static String STRING_SLASH = "/";

    // encode type
    public final static String ENCODE_BASE64 = "base64";

    // LocalDate pattern
    public final static String DATE_PATTERN = "yyyy-MM-dd";
    public final static String TIME_PATTERN = "HH:mm:ss";
    public final static String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public final static String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public final static String TIMEZONE_UTC = "UTC";
    public final static String TIMEZONE_KR = "Asia/Seoul";
    public static String SECURITY_AUTH_CLAIMS_KEY = "abacus_user_auth"; // auth claims key
    public static String TOKEN_CACHE_NAME = "token_cache_info"; // 캐시명

    public final static String DEFAULT_USR_GROUP_ID = "DEFAULT";
    public final static int DEFAULT_PAGE = 1;

    public final static String CUD_C = "C";
    public final static String CUD_U = "U";
    public final static String CUD_D = "D";



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
    /* 사장님 역할 (사장님으로 가입한 사용자, /owner/** 사용) */
    public static final String ROLE_OWNER = "ROLE_OWNER";

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

    // common status
    public static final String FAIL = "FAIL";
    public static final String SUCCESS = "SUCCESS";
    public static final String AUTH = "AUTH";

}
