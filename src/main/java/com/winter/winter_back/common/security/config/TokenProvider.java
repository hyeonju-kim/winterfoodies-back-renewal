package com.winter.winter_back.common.security.config;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.auth.dto.TokenVo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * description    : JWT 토큰 관리 유틸리티
 *
 *          1. JWT 토큰 생성 (Access Token + Refresh Token)
 *          2. JWT 토큰 검증 (서명, 만료시간 확인)
 *          3. JWT 토큰에서 사용자 인증 정보 추출
 *          4. HTTP 요청 헤더에서 토큰 추출
 *
 * fileName       : TokenProvider
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 */
@Component
@Slf4j
public class TokenProvider {

    // JWT 서명에 사용할 비밀키 (HMAC-SHA512 알고리즘용)
    private static Key key;

    // JWT Claims에서 권한 정보를 저장할 키 이름
    private static String authKey;

    // Access Token 만료 시간 (밀리초)
    private static Long accessTokenExpireTime;

    // Refresh Token 만료 시간 (밀리초)
    private static Long refreshTokenExpireTime;

    // HTTP Authorization 헤더 이름
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Bearer 토큰 접두사
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * TokenProvider 초기화 메서드
     * SecurityConfig에서 서버 시작 시 한 번 호출하여 JWT 설정 값들을 초기화
     *
     * @param secretKey JWT 서명용 Base64 인코딩된 비밀키
     * @param claimKey JWT Claims에서 권한 정보 저장 시 사용할 키
     * @param accessTokenExpireMinute Access Token 만료 시간 (분 단위)
     * @param refreshTokenExpireMinute Refresh Token 만료 시간 (분 단위)
     */
    public static void build(String secretKey, String claimKey, int accessTokenExpireMinute, int refreshTokenExpireMinute) {
        // Base64로 인코딩된 비밀키를 디코딩하여 HMAC-SHA512용 Key 객체 생성
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        authKey = claimKey;

        // 분 단위를 밀리초로 변환하여 저장
        accessTokenExpireTime = (long) (1000 * 60 * accessTokenExpireMinute);
        refreshTokenExpireTime = (long) (1000 * 60 * refreshTokenExpireMinute);
    }

    /**
     * TokenProvider가 정상적으로 초기화되었는지 확인
     *
     * @return 초기화 여부 (true: 초기화 완료, false: 초기화 필요)
     */
    public static boolean isBuild() {
        return key != null && StringUtils.hasText(authKey) &&
                accessTokenExpireTime > 0 && refreshTokenExpireTime > 0;
    }

    /**
     * 사용자 인증 정보를 기반으로 JWT 토큰 쌍(Access Token + Refresh Token) 생성
     *
     * @param authentication Spring Security의 인증 객체 (사용자명, 권한 정보 포함)
     * @return TokenVo 객체 (accessToken, refreshToken 포함)
     * @throws BizException TokenProvider가 초기화되지 않은 경우
     */
    public static TokenVo makeTokenVo(Authentication authentication) {
        if (!isBuild()) {
            throw new BizException("token_not_build");
        }

        long curTime = new Date().getTime();

        // 사용자의 권한 정보를 콤마로 구분된 문자열로 변환
        // 예: "ROLE_ADMIN,ROLE_USER" 형태
        String authoritiesString = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Principal에서 userId 추출 - 객체 타입에 따라 처리
        String userId;
        if (authentication.getPrincipal() instanceof ComLoginUserInfoDto) {
            ComLoginUserInfoDto userInfo = (ComLoginUserInfoDto) authentication.getPrincipal();
            userId = userInfo.getUserId();
            log.info("[makeTokenVo] > [userId from ComLoginUserInfoDto] === {}", userId);
        } else {
            // 문자열 형태의 principal인 경우 (토큰 갱신 시)
            userId = authentication.getName();
            log.info("[makeTokenVo] > [userId from getName] === {}", userId);
        }

        // userId가 비어있는 경우 예외 처리
        if (!StringUtils.hasText(userId)) {
            throw new BizException("invalid_user_info");
        }

        // Access Token 생성 - 짧은 만료시간, 권한 정보 포함
        ComLoginUserInfoDto userInfo = (ComLoginUserInfoDto) authentication.getPrincipal();

        String accessToken = Jwts.builder()
                .setSubject(userId)  // 사용자 ID만 subject로 설정 (전체 객체 X)
                .setExpiration(new Date(curTime + accessTokenExpireTime))  // 만료시간 설정
                .claim(authKey, authoritiesString)  // 권한 정보를 custom claim으로 추가
                .claim("userNm", userInfo.getUserNm())
                .claim("tel", userInfo.getTel())
                .signWith(key, SignatureAlgorithm.HS512)  // HMAC-SHA512로 서명
                .compact();  // JWT 문자열로 직렬화

        // Refresh Token 생성 - 긴 만료시간, 권한 정보 없음 (최소한의 정보만)
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(curTime + refreshTokenExpireTime))  // 만료시간만 설정
                .signWith(key, SignatureAlgorithm.HS512)  // 동일한 키로 서명
                .compact();

        log.info("[makeTokenVo] > JWT 토큰 생성 완료. userId: {}", userId);
        return new TokenVo(accessToken, refreshToken);
    }

    /**
     * Access Token에서 사용자 인증 정보(Authentication) 추출
     * TokenFilter에서 요청마다 호출하여 SecurityContext에 인증 정보 설정 시 사용
     *
     * @param accessToken JWT Access Token 문자열
     * @return Authentication 객체 (사용자명, 권한 정보 포함)
     * @throws BizException TokenProvider가 초기화되지 않았거나 토큰 파싱 실패 시
     */
    public static Authentication getAuthentication(String accessToken) {
        log.info("[getAuthentication] > [accessToken] === {}", accessToken);

        if (!isBuild()) {
            throw new BizException("token_not_build");
        }

        // JWT에서 Claims (페이로드) 추출
        Claims claims = parseClaims(accessToken);
        log.info("[getAuthentication] > [claims] === {}", claims);


        // Claims에서 권한 정보 문자열 추출
        String authoritiesString = claims.get(authKey).toString();

        // 콤마로 구분된 권한 문자열을 GrantedAuthority 컬렉션으로 변환
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(authoritiesString.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        log.info("[getAuthentication] > [authorities] === {}", authorities);

        // Spring Security의 Authentication 객체 생성
        // principal: User 객체 (사용자명, 비밀번호는 빈 문자열, 권한 목록)
        // credentials: 빈 문자열 (JWT에서는 비밀번호 불필요)
        // authorities: 권한 목록
        ComLoginUserInfoDto loginUserInfoDto = new ComLoginUserInfoDto();
        loginUserInfoDto.setUserId(claims.getSubject());
        loginUserInfoDto.setUserNm(claims.get("userNm", String.class));
        loginUserInfoDto.setTel(claims.get("tel", String.class));

        return new UsernamePasswordAuthenticationToken(
                loginUserInfoDto, "", authorities);
    }

    /**
     * JWT 토큰의 유효성 검증
     * 서명 검증, 만료시간 확인, 토큰 형식 검증 등을 수행
     *
     * @param token 검증할 JWT 토큰 문자열
     * @return 검증 결과 코드
     *         0: 정상 (유효한 토큰)
     *         -1: TokenProvider 초기화 안됨
     *         1: 잘못된 서명
     *         2: 만료된 토큰
     *         3: 지원하지 않는 토큰
     *         4: 잘못된 토큰 형식
     */
    public static int validToken(String token) {
        if (!isBuild()) {
            return -1; // TokenProvider가 초기화되지 않음
        }
        try {
            // JWT 파싱 및 서명 검증 시도
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return 0; // 정상 - 유효한 토큰
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            return 1; // 잘못된 서명 또는 토큰 형식
        } catch (ExpiredJwtException e) {
            return 2; // 만료된 토큰
        } catch (UnsupportedJwtException e) {
            return 3; // 지원하지 않는 JWT 토큰
        } catch (IllegalArgumentException e) {
            return 4; // 잘못된 인자 (토큰이 null이거나 빈 문자열 등)
        }
    }

    /**
     * JWT 토큰에서 Claims (페이로드) 정보 추출
     *
     * @param accessToken JWT Access Token 문자열
     * @return Claims 객체 (사용자명, 권한, 만료시간 등의 정보 포함)
     * @throws BizException TokenProvider가 초기화되지 않은 경우
     */
    private static Claims parseClaims(String accessToken) {
        if (!isBuild()) {
            throw new BizException("token_not_build");
        }
        try {
            // 정상적인 토큰 파싱
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이라도 Claims는 추출 가능 (토큰 갱신 시 사용자 정보 확인용)
            return e.getClaims();
        }
    }

    /**
     * 현재 SecurityContext에서 인증 정보 조회
     *
     * @return 현재 요청의 Authentication 객체 (로그인된 사용자 정보)
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 Access Token 추출
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰 문자열 (Bearer 접두사 제거된 순수 토큰)
     *         Authorization 헤더가 없거나 Bearer 형식이 아니면 null 반환
     */
    public static String getAccessTokenFromHeader(HttpServletRequest request) {
        // Authorization 헤더 값 추출
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        log.info("[getAccessTokenFromHeader] > [bearerToken] === {}", bearerToken);

        // "Bearer "로 시작하는지 확인 후 토큰 부분만 추출
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            log.info("[getAccessTokenFromHeader] > [bearerToken] === {}", bearerToken);
            log.info("[getAccessTokenFromHeader] > [bearerToken.bearerToken.substring(7)] === {}", bearerToken.substring(7));
            return bearerToken.substring(7);  // "Bearer " (7글자) 제거 후 토큰 반환
        }
        return null;  // 유효한 Bearer 토큰이 없음
    }
}