package com.winter.winter_back.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description    : 토큰 객체
 * fileName       : TokenVo
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVo {
    private String accessToken;
    private String refreshToken;
}