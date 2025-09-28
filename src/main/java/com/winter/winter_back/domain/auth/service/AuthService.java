package com.winter.winter_back.domain.auth.service;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.common.security.config.TokenProvider;
import com.winter.winter_back.common.security.dao.SecuritySupportDao;
import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.common.security.dto.ComLoginUserMenuDto;
import com.winter.winter_back.common.security.dto.ComRoleUserDto;
import com.winter.winter_back.common.security.dto.RoleMenuScrnMapg;
import com.winter.winter_back.domain.auth.dto.LoginRequest;
import com.winter.winter_back.domain.auth.dto.RefreshTokenRequest;
import com.winter.winter_back.domain.auth.dto.TokenVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * description    : JWT 토큰 기반 인증 서비스
 * fileName       : AuthService
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final SecuritySupportDao securitySupportDao;  // 사용자/권한 정보 조회
    private final PasswordEncoder passwordEncoder;        // 비밀번호 암호화/검증
    private final CacheManager cacheManager;             // 토큰 정보 캐시 관리

    /**
     * 사용자 로그인 처리 및 JWT 토큰 발급
     *
     * 처리 순서:
     * 1. 사용자 존재 여부 확인
     * 2. 비밀번호 검증
     * 3. 사용자 권한 정보 조회
     * 4. 메뉴 권한 정보 조회
     * 5. 로그인 이력 저장
     * 6. JWT 토큰 생성 및 캐시 저장
     *
     * @param loginRequest 로그인 요청 정보 (username, password)
     * @return TokenVo JWT 토큰 쌍 (accessToken, refreshToken)
     * @throws BizException 사용자 없음, 비밀번호 불일치 등의 경우
     */
    public TokenVo login(LoginRequest loginRequest) {
        // 1. 사용자 기본 정보 조회 (ID, 이름, 연락처 등)
        ComLoginUserInfoDto userInfo = securitySupportDao.selectUser(loginRequest.getUsername());
        log.info("[login] > [userInfo] === {}", userInfo);

        if (userInfo == null) {
            log.error("User not found: {}", loginRequest.getUsername());
            throw new BizException("user_not_found");
        }

        // 2. 비밀번호 검증 - 암호화된 비밀번호와 평문 비밀번호 비교
        String encPassword = securitySupportDao.selectUserEncPswd(loginRequest.getUsername());
        if (!passwordEncoder.matches(loginRequest.getPassword(), encPassword)) {
            log.error("Password mismatch for user: {}", loginRequest.getUsername());
            throw new BizException("invalid_password");
        }

        // 3. 사용자 권한 정보 조회 (ROLE_ADMIN, ROLE_USER 등)
        List<ComRoleUserDto> roleList = securitySupportDao.getLoginUserRole(userInfo.getUserId());
        log.info("[login] > [roleList] === {}", roleList);

        if (roleList == null || roleList.isEmpty()) {
            log.warn("사용자 {} 의 권한이 없습니다.", userInfo.getUserId());
            roleList = new ArrayList<>();
        }
        userInfo.setRoleList(roleList);

        // 4. 메뉴 권한 정보 조회 (사용자가 접근 가능한 메뉴 목록)
        List<RoleMenuScrnMapg> menuList = securitySupportDao.getRoleMenuScrnRoleUse(roleList);
        log.info("[login] > [menuList] === {}", menuList);
        userInfo.setMenuScreenInfo(convertToMenuDto(menuList));

        // 5. 로그인 이력 저장 (로그인 시간, 사용자 ID 등)
        securitySupportDao.insertLoginUser(userInfo.getUserId());

        // 6. JWT 토큰 생성 및 반환
        return tokenManage(userInfo.getUserId(), userInfo);
    }

    /**
     * JWT 토큰 갱신 처리
     *
     * Access Token이 만료되었을 때 Refresh Token을 사용하여 새로운 토큰 쌍을 발급
     *
     * 처리 순서:
     * 1. Refresh Token 유효성 검증
     * 2. 기존 Access Token에서 사용자 정보 추출
     * 3. 캐시에서 저장된 토큰 정보와 비교 검증
     * 4. 새로운 토큰 쌍 생성
     * 5. 캐시 업데이트
     *
     * @param request HTTP 요청 (Authorization 헤더에서 Access Token 추출용)
     * @param refreshRequest 토큰 갱신 요청 (accessToken, refreshToken 포함)
     * @return TokenVo 새로운 JWT 토큰 쌍
     * @throws BizException 토큰 만료, 토큰 불일치, 캐시 정보 없음 등의 경우
     */
    public TokenVo refreshToken(HttpServletRequest request, RefreshTokenRequest refreshRequest) {
        // 1. Refresh Token 유효성 검증 (만료, 서명 등)
        int valid = TokenProvider.validToken(refreshRequest.getRefreshToken());
        log.info("[refreshToken] > [valid] === {}", valid);

        if (valid != 0) {
            throw new BizException("token_refresh_expire");
        }

        // 2. Access Token 추출 (헤더 또는 요청 body에서)
        String accessToken = TokenProvider.getAccessTokenFromHeader(request);
        log.info("[refreshToken] > [accessToken] === {}", accessToken);

        if (!StringUtils.hasText(accessToken)) {
            accessToken = refreshRequest.getAccessToken();
            if (!StringUtils.hasText(accessToken)) {
                throw new BizException("param_required");
            }
        }

        // 3. 만료된 Access Token에서 사용자 정보 추출 (만료되어도 사용자명은 추출 가능)
        Authentication authentication = TokenProvider.getAuthentication(accessToken);
        log.info("[refreshToken] > [authentication] === {}", authentication);

        if (authentication == null) {
            throw new BizException("token_wrong");
        }

        // 4. 사용자 ID 추출 - principal의 타입에 따라 처리
        String userId;
        if (authentication.getPrincipal() instanceof ComLoginUserInfoDto) {
            ComLoginUserInfoDto userInfo = (ComLoginUserInfoDto) authentication.getPrincipal();
            userId = userInfo.getUserId();
            log.info("[refreshToken] > [userId from ComLoginUserInfoDto] === {}", userId);
        } else {
            userId = authentication.getName();
            log.info("[refreshToken] > [userId from getName] === {}", userId);
        }

        if (!StringUtils.hasText(userId)) {
            throw new BizException("token_wrong");
        }

        // 5. 캐시에서 해당 사용자의 토큰 정보 조회
        Cache winterCache = cacheManager.getCache("winter_cache");
        log.info("[refreshToken] > [winterCache] === {}", winterCache.getName());

        Cache.ValueWrapper valueWrapper = winterCache.get(userId);
        log.info("[refreshToken] > [valueWrapper] === {}", valueWrapper);

        if (valueWrapper == null) {
            log.warn("[refreshToken] > 캐시에서 사용자 {} 의 토큰 정보를 찾을 수 없습니다.", userId);
            throw new BizException("token_empty_auth");
        }

        // 6. 캐시에서 조회한 토큰 정보 추출
        TokenVo cachedToken = (TokenVo) valueWrapper.get();
        if (cachedToken == null) {
            log.warn("[refreshToken] > 캐시의 토큰 정보가 null입니다. userId: {}", userId);
            throw new BizException("token_empty_auth");
        }

        // 7. 요청받은 Refresh Token과 캐시에 저장된 Refresh Token 일치 여부 확인
        if (!refreshRequest.getRefreshToken().equals(cachedToken.getRefreshToken())) {
            log.error("[refreshToken] > Refresh Token 불일치. userId: {}", userId);
            throw new BizException("token_refresh_wrong");
        }

        // 8. 사용자 정보 재조회 (새로운 토큰 생성을 위해)
        ComLoginUserInfoDto userInfo = securitySupportDao.selectUser(userId);
        if (userInfo == null) {
            log.error("[refreshToken] > 사용자를 찾을 수 없습니다: {}", userId);
            throw new BizException("user_not_found");
        }

        // 9. 사용자 권한 정보 재조회
        List<ComRoleUserDto> roleList = securitySupportDao.getLoginUserRole(userId);
        if (roleList == null || roleList.isEmpty()) {
            log.warn("[refreshToken] > 사용자 {} 의 권한이 없습니다.", userId);
            roleList = new ArrayList<>();
        }
        userInfo.setRoleList(roleList);

        // 10. 메뉴 권한 정보 재조회 (필요한 경우)
        List<RoleMenuScrnMapg> menuList = securitySupportDao.getRoleMenuScrnRoleUse(roleList);
        userInfo.setMenuScreenInfo(convertToMenuDto(menuList));

        // 11. 새로운 토큰 쌍 생성
        TokenVo newToken = tokenManage(userId, userInfo);
        log.info("[refreshToken] > [newToken] === {}", newToken);

        log.info("[refreshToken] > 토큰 갱신 완료. userId: {}", userId);
        return newToken;
    }

    /**
     * JWT 토큰 생성 및 캐시 저장을 담당하는 내부 메서드
     *
     * @param userId 사용자 ID (캐시 키로 사용)
     * @param userInfo 사용자 정보 (권한 목록 포함)
     * @return TokenVo 생성된 JWT 토큰 쌍
     */
    private TokenVo tokenManage(String userId, ComLoginUserInfoDto userInfo) {
        log.info("[tokenManage] > [userId] === {}", userId);
        log.info("[tokenManage] > [userInfo] === {}", userInfo);

        // 1. 사용자 권한을 Spring Security의 GrantedAuthority 형태로 변환
        // 예: ComRoleUserDto의 roleId "ROLE_ADMIN" -> SimpleGrantedAuthority("ROLE_ADMIN")
        List<GrantedAuthority> authorities = userInfo.getRoleList().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleId()))
                .collect(Collectors.toList());

        // 2. Spring Security Authentication 객체 생성
        // principal: 사용자 정보 객체
        // credentials: null (JWT에서는 비밀번호 불필요)
        // authorities: 권한 목록
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userInfo, null, authorities);

        // 3. JWT 토큰 쌍 생성 (Access Token + Refresh Token)
        TokenVo tokenVo = TokenProvider.makeTokenVo(authentication);

        // 4. 캐시에 토큰 정보 저장
        // 키: 사용자 ID, 값: TokenVo 객체
        // 토큰 갱신 시 검증용으로 사용
        Objects.requireNonNull(cacheManager.getCache("winter_cache"))
                .put(userId, tokenVo);

        return tokenVo;
    }

    /**
     * RoleMenuScrnMapg를 ComLoginUserMenuDto로 변환하는 메서드
     *
     * 현재는 임시 구현이며, 실제 프로젝트에서는 메뉴 정보 변환 로직 구현 필요
     *
     * @param menuList 데이터베이스에서 조회한 메뉴 권한 정보
     * @return 변환된 메뉴 정보 리스트
     */
    private List<ComLoginUserMenuDto> convertToMenuDto(List<RoleMenuScrnMapg> menuList) {
        // TODO: 실제 변환 로직 구현 필요
        // 현재는 빈 리스트 반환 (메뉴 기능 미구현)
        return new ArrayList<>();
    }


    /**
     * 로그아웃 처리 (캐시 및 토큰 무효화)
     */
    public void logout(String userId) {
        try {
            // 1. 사용자의 모든 토큰 무효화
            TokenProvider.invalidateAllUserTokens(userId);

            // 2. 캐시에서 토큰 정보 삭제
            Cache winterCache = cacheManager.getCache("winter_cache");
            if (winterCache != null) {
                winterCache.evict(userId);
                log.info("[logout] 사용자 {}의 캐시 정보가 삭제되었습니다", userId);
            }

            log.info("[logout] 사용자 {} 로그아웃 완료", userId);
        } catch (Exception e) {
            log.error("[logout] 로그아웃 처리 중 오류 발생: {}", e.getMessage());
            throw new BizException("logout_failed");
        }
    }
}