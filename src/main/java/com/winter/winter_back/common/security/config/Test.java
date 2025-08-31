package com.winter.winter_back.common.security.config;

import com.winter.winter_back.common.dto.ResponseBaseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * description    : 세션 체크 등 테스트용 파일
 * packageName    : kr.co.iabacus.autodriving.common.security.config
 * fileName       : Test
 * author         : 김현주
 * date           : 25. 5. 14.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.        김현주             최초 생성
 */

@RestController
@RequiredArgsConstructor
public class Test {

    @GetMapping("/session-check")
    public void sessionCheck(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("세션 ID: " + session.getId());
            System.out.println("생성 시간: " + session.getCreationTime());
            System.out.println("세션 클래스: " + session.getClass().getName());

            // org.apache.catalina.session.StandardSession → Tomcat 기본 세션
            // org.springframework.session.web.http.SessionRepositoryFilter$SessionRepositoryRequestWrapper$HttpSessionWrapper -> Spring Redis Session
        } else {
            System.out.println("세션 없음");
        }
    }

    @GetMapping("/vehicle/hi")
    public String test(String username) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String encodedPassword = encoder.encode(username);
        System.out.println("유저 네임: " + username);
        System.out.println("암호화된 비밀번호: " + encodedPassword);
        return encodedPassword;
    }

    @GetMapping("/hi2")
    public String test2() {
        return "hi2";
    }

    @GetMapping("/session-expired")
    public ResponseEntity<ResponseBaseDto> sessionExpired() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseBaseDto("40100001", "세션이 만료되었습니다", 401, null));
    }



}
