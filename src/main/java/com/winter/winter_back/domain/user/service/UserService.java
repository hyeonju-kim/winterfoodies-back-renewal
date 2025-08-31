package com.winter.winter_back.domain.user.service;

import com.winter.winter_back.domain.user.dto.UserDto;

/**
 * description    :
 * fileName       : UserService
 * author         : hyeonju
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hyeonju             최초 생성
 */
public interface UserService {
    void register(UserDto userDto);

}
