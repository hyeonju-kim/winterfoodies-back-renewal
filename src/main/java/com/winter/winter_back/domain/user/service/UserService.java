package com.winter.winter_back.domain.user.service;

import com.winter.winter_back.domain.user.dto.PasswordChangeRequest;
import com.winter.winter_back.domain.user.dto.UserDto;
import com.winter.winter_back.domain.user.dto.UserProfileUpdateRequest;
import com.winter.winter_back.domain.user.dto.WithdrawRequest;

/**
 * description    : 유저 서비스 인터페이스
 * fileName       : UserService
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 * 26. 10. 26.       hj kim             내 정보 조회·수정, 비밀번호 변경, 회원탈퇴 추가
 */
public interface UserService {
    void register(UserDto userDto); // 회원가입

    UserDto getMyProfile(String userId); // 내 정보 (DB 기준 최신 값)

    UserDto modMyProfile(String userId, UserProfileUpdateRequest request); // 이름·전화번호 수정

    void modMyPassword(String userId, PasswordChangeRequest request); // 비밀번호 변경 (모든 기기 로그아웃)

    void delMyAccount(String userId, WithdrawRequest request); // 회원탈퇴 (모든 기기 로그아웃)
}
