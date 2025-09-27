package com.winter.winter_back.domain.auth.dto;

import lombok.Data;

/**
 * description    : 리프레시 토큰 객체
 * fileName       : RefreshTokenRequest
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 */
@Data
public class RefreshTokenRequest {
    private String accessToken;
    private String refreshToken;
}