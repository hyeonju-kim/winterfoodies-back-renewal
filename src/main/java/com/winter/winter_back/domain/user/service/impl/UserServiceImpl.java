package com.winter.winter_back.domain.user.service.impl;

import com.winter.winter_back.common.dto.BaseConstant;
import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.auth.service.AuthService;
import com.winter.winter_back.domain.user.dao.UserDao;
import com.winter.winter_back.domain.user.dto.PasswordChangeRequest;
import com.winter.winter_back.domain.user.dto.UserDto;
import com.winter.winter_back.domain.user.dto.UserProfileUpdateRequest;
import com.winter.winter_back.domain.user.dto.WithdrawRequest;
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
 * 26. 10. 26.       hj kim             내 정보 조회·수정, 비밀번호 변경, 회원탈퇴 추가
 * 26. 10. 26.       hj kim             사장님으로 가입 추가
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

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

        userDto.setPw(encodedPassword);

        // 3. 권한: 사장님으로 가입하면 ROLE_OWNER (가게는 로그인 후 가게 등록에서 만든다)
        boolean isOwner = Boolean.TRUE.equals(userDto.getIsOwner());
        userDao.regUser(userDto);
        userDao.regUserAuthMap(userDto.getId(), isOwner ? BaseConstant.ROLE_OWNER : BaseConstant.ROLE_DEFAULT);
        log.info("회원가입 완료: {} ({})", userDto.getId(), isOwner ? "사장님" : "손님");
    }

    /**
     * Author      : hj kim
     * Description : 내 정보 조회. 토큰에 담긴 값은 수정 전 값일 수 있어 DB 에서 읽는다
     * Date        : 26. 10. 26.
     */
    @Override
    public UserDto getMyProfile(String userId) {
        UserDto user = userDao.getUser(userId);
        if (user == null) {
            throw new BizException("user_not_found");
        }
        return user;
    }

    /**
     * Author      : hj kim
     * Description : 이름·전화번호 수정 (전화번호를 비우면 삭제). 수정된 내 정보를 돌려준다
     * Date        : 26. 10. 26.
     */
    @Override
    @Transactional
    public UserDto modMyProfile(String userId, UserProfileUpdateRequest request) {
        String tel = request.getTel() == null || request.getTel().isBlank() ? null : request.getTel();
        if (userDao.modUserProfile(userId, request.getName().trim(), tel) == 0) {
            throw new BizException("user_not_found");
        }
        return getMyProfile(userId);
    }

    /**
     * Author      : hj kim
     * Description : 현재 비밀번호 확인 후 변경. 다른 기기에 남은 로그인도 끊기 위해 모든 토큰을 무효화한다
     * Date        : 26. 10. 26.
     */
    @Override
    @Transactional
    public void modMyPassword(String userId, PasswordChangeRequest request) {
        checkPassword(userId, request.getCurrentPassword());
        userDao.modUserPassword(userId, passwordEncoder.encode(request.getNewPassword()));
        authService.logout(userId);
        log.info("[modMyPassword] 사용자 {} 비밀번호 변경", userId);
    }

    /**
     * Author      : hj kim
     * Description : 비밀번호 확인 후 회원탈퇴. 회원은 사용 안 함(use_yn = N)으로 바꾸고 이름·전화번호를 지운다.
     *               주문·리뷰 기록은 가게 정산·통계 때문에 남기고(리뷰 작성자는 공개 목록에서 가려진다),
     *               탈퇴한 아이디는 다른 사람이 다시 쓰지 못한다
     * Date        : 26. 10. 26.
     */
    @Override
    @Transactional
    public void delMyAccount(String userId, WithdrawRequest request) {
        checkPassword(userId, request.getPassword());
        userDao.modUserWithdraw(userId);
        authService.logout(userId);
        log.info("[delMyAccount] 사용자 {} 탈퇴", userId);
    }

    private void checkPassword(String userId, String rawPassword) {
        String encodedPassword = userDao.getUserPassword(userId);
        if (encodedPassword == null) {
            throw new BizException("user_not_found");
        }
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BizException("invalid_password");
        }
    }
}
