package com.winter.winter_back.common.security.config;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.auth.dto.TokenVo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description    : JWT 토큰 관리 유틸리티 (토큰 무효화 기능 포함)
 *
 *          1. JWT 토큰 생성 (Access Token + Refresh Token)
 *          2. JWT 토큰 검증 (서명, 만료시간 확인)
 *          3. JWT 토큰에서 사용자 인증 정보 추출
 *          4. HTTP 요청 헤더에서 토큰 추출
 *          5. 토큰 무효화 (블랙리스트 관리)
 *
 * fileName       : TokenProvider
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 * 25. 9. 28.        hj kim             토큰 무효화 기능 추가
 */
@Component
@Slf4j
public class TokenProvider {

    // JWT 서명에 사용할 비밀키 (HMAC-SHA512 알고리즘용)
    private static SecretKey key;

    // JWT Claims에서 권한 정보를 저장할 키 이름
    private static String authKey;

    // Access Token 만료 시간 (밀리초)
    private static Long accessTokenExpireTime;

    // Refresh Token 만료 시간 (밀리초)
    private static Long refreshTokenExpireTime;

    // Redis 템플릿 (토큰 블랙리스트 관리용)
    private static RedisTemplate<String, String> redisTemplate;

    // HTTP Authorization 헤더 이름
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Bearer 토큰 접두사
    public static final String BEARER_PREFIX = "Bearer ";

    // 블랙리스트 Redis 키 접두사
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:token:";

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
     * Redis 템플릿 설정 (토큰 블랙리스트 관리용)
     *
     * @param template Redis 템플릿
     */
    public static void setRedisTemplate(RedisTemplate<String, String> template) {
        redisTemplate = template;
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
     * 토큰을 블랙리스트에 추가하여 무효화
     *
     * @param token 무효화할 JWT 토큰
     * @return 무효화 성공 여부
     */
    public static boolean invalidateToken(String token) {
        if (!isBuild() || redisTemplate == null) {
            log.error("[invalidateToken] TokenProvider가 초기화되지 않았거나 Redis가 설정되지 않았습니다");
            return false;
        }

        try {
            // 토큰에서 만료시간 추출
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();

            // 현재 시간과 만료시간 비교하여 TTL 계산
            long currentTime = System.currentTimeMillis();
            long expirationTime = expiration.getTime();

            if (expirationTime <= currentTime) {
                log.info("[invalidateToken] 이미 만료된 토큰입니다");
                return true; // 이미 만료된 토큰은 무효화할 필요 없음
            }

            long ttl = (expirationTime - currentTime) / 1000; // 초 단위로 변환

            // Redis에 블랙리스트로 등록 (만료시간까지만 저장)
            String blacklistKey = BLACKLIST_KEY_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, "blacklisted", Duration.ofSeconds(ttl));

            log.info("[invalidateToken] 토큰이 블랙리스트에 추가되었습니다. TTL: {}초", ttl);
            return true;

        } catch (Exception e) {
            log.error("[invalidateToken] 토큰 무효화 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 특정 사용자의 모든 토큰 무효화 (로그아웃 시 사용)
     *
     * @param userId 사용자 ID
     * @return 무효화 성공 여부
     */
    public static boolean invalidateAllUserTokens(String userId) {
        if (!isBuild() || redisTemplate == null) {
            log.error("[invalidateAllUserTokens] TokenProvider가 초기화되지 않았거나 Redis가 설정되지 않았습니다");
            return false;
        }

        try {
            // 사용자별 무효화 플래그 설정 (새로운 로그인까지 유효)
            String userInvalidationKey = "user_logout:" + userId;
            long timestamp = System.currentTimeMillis();

            // 현재 시간을 저장하여 이 시간 이전에 발급된 토큰들은 모두 무효화
            redisTemplate.opsForValue().set(userInvalidationKey, String.valueOf(timestamp),
                    Duration.ofMillis(refreshTokenExpireTime));

            log.info("[invalidateAllUserTokens] 사용자 {}의 모든 토큰이 무효화되었습니다", userId);
            return true;

        } catch (Exception e) {
            log.error("[invalidateAllUserTokens] 사용자 토큰 무효화 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     *
     * @param token 확인할 JWT 토큰
     * @return 블랙리스트 여부 (true: 무효화된 토큰, false: 유효한 토큰)
     */
    public static boolean isTokenBlacklisted(String token) {
        if (redisTemplate == null) {
            return false; // Redis가 없으면 블랙리스트 기능 비활성화
        }

        try {
            // 1. 개별 토큰 블랙리스트 확인
            String blacklistKey = BLACKLIST_KEY_PREFIX + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                log.info("[isTokenBlacklisted] 토큰이 블랙리스트에 있습니다");
                return true;
            }

            // 2. 사용자별 전체 무효화 확인
            Claims claims = parseClaims(token);
            String userId = claims.getSubject();
            Date issuedAt = claims.getIssuedAt();

            if (userId != null && issuedAt != null) {
                String userInvalidationKey = "user_logout:" + userId;
                String invalidationTimestamp = redisTemplate.opsForValue().get(userInvalidationKey);

                if (invalidationTimestamp != null) {
                    long logoutTime = Long.parseLong(invalidationTimestamp);
                    long tokenIssuedTime = issuedAt.getTime();

                    if (tokenIssuedTime < logoutTime) {
                        log.info("[isTokenBlacklisted] 사용자 로그아웃 이전에 발급된 토큰입니다");
                        return true;
                    }
                }
            }

            return false;

        } catch (Exception e) {
            log.error("[isTokenBlacklisted] 블랙리스트 확인 중 오류 발생: {}", e.getMessage());
            return false;
        }
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
        String authoritiesString = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Principal에서 userId 추출
        String userId;
        if (authentication.getPrincipal() instanceof ComLoginUserInfoDto) {
            ComLoginUserInfoDto userInfo = (ComLoginUserInfoDto) authentication.getPrincipal();
            userId = userInfo.getUserId();
            log.info("[makeTokenVo] userId from ComLoginUserInfoDto: {}", userId);
        } else {
            userId = authentication.getName();
            log.info("[makeTokenVo] userId from getName: {}", userId);
        }

        if (!StringUtils.hasText(userId)) {
            throw new BizException("invalid_token");
        }

        ComLoginUserInfoDto userInfo = (ComLoginUserInfoDto) authentication.getPrincipal();

        // Access Token 생성 - 발급시간(iat) 추가
        String accessToken = Jwts.builder()
                .subject(userId)
                .issuedAt(new Date(curTime)) // 발급시간 추가 (로그아웃 시 비교용)
                .expiration(new Date(curTime + accessTokenExpireTime))
                .claim(authKey, authoritiesString)
                .claim("userNm", userInfo.getUserNm())
                .claim("tel", userInfo.getTel())
                .signWith(key, Jwts.SIG.HS512)
                .compact();

        // Refresh Token 생성 - 발급시간(iat) 추가
        String refreshToken = Jwts.builder()
                .subject(userId) // Refresh Token에도 userId 추가
                .issuedAt(new Date(curTime)) // 발급시간 추가
                .expiration(new Date(curTime + refreshTokenExpireTime))
                .signWith(key, Jwts.SIG.HS512)
                .compact();

        log.info("[makeTokenVo] JWT 토큰 생성 완료. userId: {}", userId);
        return new TokenVo(accessToken, refreshToken);
    }

    /**
     * Access Token에서 사용자 인증 정보(Authentication) 추출
     *
     * @param accessToken JWT Access Token 문자열
     * @return Authentication 객체 (사용자명, 권한 정보 포함)
     * @throws BizException TokenProvider가 초기화되지 않았거나 토큰 파싱 실패 시
     */
    public static Authentication getAuthentication(String accessToken) {
        log.info("[getAuthentication] accessToken: {}", accessToken);

        if (!isBuild()) {
            throw new BizException("token_not_build");
        }

        // 블랙리스트 확인 (토큰 무효화 체크)
        if (isTokenBlacklisted(accessToken)) {
            log.warn("[getAuthentication] 블랙리스트에 있는 토큰입니다");
            throw new BizException("token_blacklisted");
        }

        Claims claims = parseClaims(accessToken);
        log.info("[getAuthentication] claims: {}", claims);

        // 권한이 없는 사용자는 빈 문자열로 저장되므로, 빈 값은 걸러 권한 없이 인증한다
        Object authoritiesClaim = claims.get(authKey);
        String authoritiesString = authoritiesClaim == null ? "" : authoritiesClaim.toString();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(authoritiesString.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        log.info("[getAuthentication] authorities: {}", authorities);

        ComLoginUserInfoDto loginUserInfoDto = new ComLoginUserInfoDto();
        loginUserInfoDto.setUserId(claims.getSubject());
        loginUserInfoDto.setUserNm(claims.get("userNm", String.class));
        loginUserInfoDto.setTel(claims.get("tel", String.class));

        return new UsernamePasswordAuthenticationToken(
                loginUserInfoDto, "", authorities);
    }

    /**
     * JWT 토큰의 유효성 검증 (블랙리스트 확인 포함)
     *
     * @param token 검증할 JWT 토큰 문자열
     * @return 검증 결과 코드
     *         0: 정상 (유효한 토큰)
     *         -1: TokenProvider 초기화 안됨
     *         1: 잘못된 서명
     *         2: 만료된 토큰
     *         3: 지원하지 않는 토큰
     *         4: 잘못된 토큰 형식
     *         5: 블랙리스트에 있는 토큰
     */
    public static int validToken(String token) {
        if (!isBuild()) {
            return -1; // TokenProvider가 초기화되지 않음
        }

        // 블랙리스트 확인
        if (isTokenBlacklisted(token)) {
            return 5; // 블랙리스트에 있는 토큰
        }

        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return 0; // 정상 - 유효한 토큰
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            return 1; // 잘못된 서명 또는 토큰 형식
        } catch (ExpiredJwtException e) {
            return 2; // 만료된 토큰
        } catch (UnsupportedJwtException e) {
            return 3; // 지원하지 않는 JWT 토큰
        } catch (IllegalArgumentException e) {
            return 4; // 잘못된 인자
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
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(accessToken).getPayload();
        } catch (ExpiredJwtException e) {
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
     */
    public static String getAccessTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        log.info("[getAccessTokenFromHeader] bearerToken: {}", bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}