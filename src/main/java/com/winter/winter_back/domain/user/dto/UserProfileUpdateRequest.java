package com.winter.winter_back.domain.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * description    : 내 정보 수정 요청 (이름·전화번호)
 * fileName       : UserProfileUpdateRequest
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Data
public class UserProfileUpdateRequest {
    @NotBlank(message = "이름을 입력해주세요")
    @Size(max = 50, message = "이름은 50자까지 입력할 수 있습니다")
    private String name;

    // 비우면 전화번호 삭제. 입력하면 휴대폰 번호 숫자만 (포장 주문 연락처로 미리 채워진다)
    @Pattern(regexp = "^$|^01[0-9]{8,9}$", message = "전화번호는 휴대폰 번호 숫자만 입력해주세요")
    private String tel;
}
