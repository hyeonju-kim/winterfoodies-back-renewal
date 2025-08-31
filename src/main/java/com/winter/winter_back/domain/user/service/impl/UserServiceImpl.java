package com.winter.winter_back.domain.user.service.impl;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.user.dao.UserDao;
import com.winter.winter_back.domain.user.dto.UserDto;
import com.winter.winter_back.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description    : 유저 서비스 구현체
 * fileName       : UserServiceImpl
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(UserDto userDto) {
        log.info("회원가입 시작: {}", userDto.getId());

        // 1. 중복 체크
        if (userDao.existsByUsername(userDto.getId())) {
            throw new BizException("duplicated", "아이디");

        }

        // 2. 비밀번호 암호화해서 저장
        String encodedPassword = passwordEncoder.encode(userDto.getPw());
        log.info("[register] > [encodedPassword] === {}", encodedPassword);

        userDto.setPw(encodedPassword);

        userDao.regUser(userDto);
        userDao.regUserAuthMap(userDto.getId());
        log.info("회원가입 완료: {}", userDto.getId());
    }

}
