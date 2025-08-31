package com.winter.winter_back.domain.user.dao;

import com.winter.winter_back.domain.user.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * description    : 유저 DAO
 * fileName       : UserDao
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Mapper
public interface UserDao {
    boolean existsByUsername(String username);
    void regUser(UserDto userDto);
    boolean regUserAuthMap(String username);
}
