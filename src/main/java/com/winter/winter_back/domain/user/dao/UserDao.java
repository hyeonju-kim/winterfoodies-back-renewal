package com.winter.winter_back.domain.user.dao;

import com.winter.winter_back.domain.user.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * description    : 유저 DAO
 * fileName       : UserDao
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 * 26. 10. 26.       hj kim             내 정보 조회·수정, 비밀번호 변경, 회원탈퇴 추가
 */
@Mapper
public interface UserDao {
    boolean existsByUsername(String username); // 아이디 사용 여부 (탈퇴한 아이디도 포함 → 재사용 불가)
    void regUser(UserDto userDto); // 회원가입
    boolean regUserAuthMap(String username); // 기본 권한(ROLE_USER) 할당

    UserDto getUser(@Param("userId") String userId); // 사용 중인 회원의 아이디·이름·전화번호
    String getUserPassword(@Param("userId") String userId); // 사용 중인 회원의 암호화된 비밀번호
    int modUserProfile(@Param("userId") String userId, @Param("name") String name, @Param("tel") String tel); // 이름·전화번호 수정
    int modUserPassword(@Param("userId") String userId, @Param("pw") String encodedPassword); // 비밀번호 변경
    int modUserWithdraw(@Param("userId") String userId); // 회원탈퇴 (사용 안 함 + 이름·전화번호 삭제)
}
