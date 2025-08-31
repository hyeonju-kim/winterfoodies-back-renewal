package com.winter.winter_back.domain.user.service;

import com.winter.winter_back.domain.user.dto.UserDto;

/**
 * description    : 유저 서비스 인터페이스
 * fileName       : UserService
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
public interface UserService {
    void register(UserDto userDto);

}
