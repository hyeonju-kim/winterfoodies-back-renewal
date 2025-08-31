package com.winter.winter_back.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import com.winter.winter_back.common.security.constant.ComConstantAuth;
import com.winter.winter_back.common.security.dto.ComSecurityInfoDto;
import com.winter.winter_back.common.security.svc.SecuritySupportSvc;
import com.winter.winter_back.common.security.svc.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Map;


@Configuration
@EnableWebSecurity
@EnableGlobalAuthentication
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final ComSecurityInfoDto comSecurityInfoDto; // 1) 보안 관련 설정 정보를 담고 있는 DTO 클래스
    private final AuthenticationEntryPointImpl authenticationEntryPointImpl; // 2) 로그인되지 않은 상태에서 URL에 접근할 경우 처리하는 클래스 (인증 실패 시)
    private final AuthAccessDeniedHandler authAccessDeniedHandler; // 3) 권한이 없는 URL에 접근할 경우 처리하는 핸들러 (인가 실패 시)
    private final AuthLoginFailHandler authLoginFailureHandler; // 4) 로그인 실패 시 처리 (잘못된 아이디나 비밀번호 입력 시)
    private final AuthLoginSuccessHandler authLoginSuccessHandler; // 5) 인증 성공 시 처리
    private final AuthLogoutHandler authLogoutHandler; // 6) 로그아웃 성공 시 처리
    private final AuthLoginProvider authProvider; // 7) 인증 처리 클래스 (실제로 인증을 실행하는 핵심 클래스)
    private final SecuritySupportSvc securitySupportSvc; // 8) 사용자 인증과 권한 처리를 위한 서비스 인터페이스
    private final ExceptionWrapperUtil exceptionInfoConfig;
    private final ObjectMapper objectMapper;

    // 1. 웹 시큐리티 설정 (정적파일 설정)
    @Override
    public void configure(WebSecurity web) {
        // 보안 필터를 무시할 경로 설정, 예: 정적 파일
        web.ignoring().antMatchers(comSecurityInfoDto.getStaticContentsUrlPatterns());
    }


//     2. Http 시큐리티 설정 (보안 설정)
    @Override
    protected void configure(HttpSecurity http) throws Exception { // 스프링이 구동될 때 바로 호출되는 부분 (Spring Security가 SecurityFilterChain 만들기 위해 제일 먼저 호출, 이 Chain이 DispatcherServlet 앞단에 필터로 등록되고 이후 모든 HTTP 요청은 이 필터체인을 거침)
        // 2-1) 인증 없이 접근 가능한 URL
        http.authorizeRequests()
                .antMatchers(comSecurityInfoDto.getPermitAllUrlPatterns()).permitAll(); // 누구든 접근 허용


        // 2-2) 역할 기반으로 URL 패턴을 동적으로 구성
        Map<String, HashSet<String>> roleApiAuthMap = securitySupportSvc.getRoleApiAuthMapgForSpringSecurity("MGMT"); // 관리자용 (Management)
        for (Map.Entry<String, HashSet<String>> entry : roleApiAuthMap.entrySet()) {
            String urlPattern = entry.getKey();
            String accessExpression = getAccessString(urlPattern); // AuthorizationChecker.check() 로그인 성공한 유저의 세션에서 가져온 권한을 하나씩 확인하며 체크
            http.authorizeRequests().antMatchers(urlPattern).access(accessExpression);
        }


        // 2-3) 인증된 모든 역할의 사용자가 접근할 수 있는 URL
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, comSecurityInfoDto.getFullyAuthenticatedUrlPatterns()).fullyAuthenticated(); // 완전히 인증된 사용자만 접근 허용 (로그인 성공해서 SecurityContext에 Authentication 객체가 정상적으로 들어있는 상태여야 함)

        // 2-4) 기본 역할 기반 접근 제어
        http.authorizeRequests()
                .antMatchers("/**")
                .access(String.format("hasRole('%s')", ComConstantAuth.ROLE_TOP)); // 지정된 역할(여기서는 ROLE_TOP : "ROLE_SYSTEM")을 가진 사용자만 이 URL 패턴에 접근할 수 있도록 설정

        // 2-5) 로그인 및 로그아웃 설정
        http.
				formLogin()
//            	     .loginPage(comSecurityInfoDto.getLogin().getLoginPageUrl()) // 로그인 페이지 URL
					.loginPage("/login") // 로그인 페이지 URL (GET 방식, 로그인 폼을 보여주는 페이지)
                .loginProcessingUrl(comSecurityInfoDto.getLogin().getProcessingUrl()) // 로그인 성공 후 처리 URL (POST 방식, 아이디/비번을 제출해서 로그인 처리하는 엔드포인트)
                	 .failureHandler(authLoginFailureHandler) // 로그인 실패 핸들러
               		 .successHandler(authLoginSuccessHandler) // 로그인 성공 핸들러
//               		 .usernameParameter(comSecurityInfoDto.getLogin().getUser().getIdParamName()) // 사용자 ID 파라미터(info1)
//               		 .passwordParameter(comSecurityInfoDto.getLogin().getUser().getPwdParamName()) // 비밀번호 파라미터(info2)
               		 .and()
				.exceptionHandling()
					.authenticationEntryPoint(authenticationEntryPointImpl) // 인증 실패 시 처리할 엔트리 포인트
                	.accessDeniedHandler(authAccessDeniedHandler) // 접근 거부 시 처리할 핸들러
                	.and()
                .logout()
                	.logoutSuccessHandler(authLogoutHandler) // 로그아웃 성공 핸들러
                	.logoutRequestMatcher(new AntPathRequestMatcher(comSecurityInfoDto.getLogout().getReqUrl())) // 로그아웃 요청 URL
                	.invalidateHttpSession(true) // 로그아웃 시 세션 무효화
                	.and()
                .authenticationProvider(authProvider) // DB에서 사용자 조회 + 비밀번호 검증 → 검증 되면, 인증 정보가 SecurityContext에 저장됨 (핵심)
//                .securityContext()
//                	.securityContextRepository(customSecurityContextRepository()) // 여기에 커스텀 SecurityContextRepository를 설정 240807
//                	.and()
                .csrf().disable() // CSRF 보호 비활성화
                .sessionManagement() // 세션 관리
                    .maximumSessions(1) // 하나의 세션만 허용
                    .maxSessionsPreventsLogin(false) // 새 로그인 허용하고 기존 세션 끊기
                    .expiredSessionStrategy(event -> {
                        log.warn("[세션 만료] 이전 세션이 끊겼습니다: {}", event.getSessionInformation().getPrincipal());
                        HttpServletResponse response = event.getResponse();
                        ResponseBaseDto comResultDto = exceptionInfoConfig.makeResponse("login_required");
                        response.setStatus(comResultDto.getStatus()); // 401
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().print(objectMapper.writeValueAsString(comResultDto));
                        response.getWriter().flush();
                    });
    }



    // 시큐리티 잠시 꺼두는 용도로 추가 250514HJ
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .anyRequest().permitAll() //  모든 요청 인증 없이 허용
//                .and()
//                .formLogin().disable()    //  로그인 기능 비활성화
//                .logout().disable()       //  로그아웃 기능도 끔
//                .csrf().disable();        //  CSRF 끄기 (Postman 테스트 등 위해)
//    }

    // 3. 특정 URL 패턴에 대해 권한 검사를 수행
    private String getAccessString(String urlPattern) {
        // URL 패턴에 대한 접근 권한 표현식 생성
        return String.format("@authorizationChecker.check('%s')", urlPattern);
    }

    // 4. 인증매니저 빈 생성
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

//    @Bean
//    public HttpSessionSecurityContextRepository customSecurityContextRepository() {
//        return new HttpSessionSecurityContextRepository();
//    }
}
