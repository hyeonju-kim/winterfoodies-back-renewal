package com.winter.winter_back.domain.auth.dto;

import lombok.Data;

/**
 * description    : 로그인 요청 객체
 * fileName       : LoginRequest
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}