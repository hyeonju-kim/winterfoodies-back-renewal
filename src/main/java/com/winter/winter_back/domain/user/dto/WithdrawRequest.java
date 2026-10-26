package com.winter.winter_back.domain.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * description    : 회원탈퇴 요청 (본인 확인용 비밀번호)
 * fileName       : WithdrawRequest
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Data
public class WithdrawRequest {
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
