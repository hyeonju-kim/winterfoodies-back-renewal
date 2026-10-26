package com.winter.winter_back.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * description    : 유저 DTO
 * fileName       : UserDto
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 * 26. 10. 26.       hj kim             내 정보 응답에 사장님 여부·가게 ID 추가
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    @NotBlank(message = "아이디를 입력해주세요")
    private String id;
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String pw;
    @NotBlank(message = "이름을 입력해주세요")
    private String name;
    private String tel;

    // 내 정보 조회 응답 전용: 사장님 계정이면 true 와 가게 ID (앱이 사장님 화면을 띄울지 판단)
    private Boolean isOwner;
    private Long storeId;
}
