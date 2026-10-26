package com.winter.winter_back.domain.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * description    : 비밀번호 변경 요청
 * fileName       : PasswordChangeRequest
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Data
public class PasswordChangeRequest {
    @NotBlank(message = "현재 비밀번호를 입력해주세요")
    private String currentPassword;

    // 회원가입 화면과 같은 기준 (8자 이상)
    @NotBlank(message = "새 비밀번호를 입력해주세요")
    @Size(min = 8, max = 20, message = "새 비밀번호는 8~20자로 입력해주세요")
    private String newPassword;
}
