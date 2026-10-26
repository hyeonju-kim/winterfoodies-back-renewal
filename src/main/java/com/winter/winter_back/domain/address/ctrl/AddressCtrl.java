package com.winter.winter_back.domain.address.ctrl;

import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.address.dto.AddressDto;
import com.winter.winter_back.domain.address.dto.AddressListResponse;
import com.winter.winter_back.domain.address.svc.AddressSvc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * description    : 주소 컨트롤러
 * fileName       : AddressCtrl
 * author         : hj kim
 * date           : 25. 8. 30.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 30.        hj kim             최초 생성
 */


@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressCtrl {

    private final AddressSvc addressSvc;

    /**
     * 주소 목록 조회
     */
    @GetMapping
    public AddressListResponse getAddressList(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        log.info("[getAddressList] > [loginUserInfo] === {}", loginUserInfo);

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return addressSvc.getAddressList(loginUserInfo.getUserId());
    }

    /**
     * 주소 상세 조회
     */
    @GetMapping("/{addressId}")
    public AddressDto getAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return addressSvc.getAddress(addressId, loginUserInfo.getUserId());
    }

    /**
     * 주소 등록
     */
    @PostMapping
    public AddressDto createAddress(
            @Valid @RequestBody AddressDto request,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return addressSvc.regAddress(request, loginUserInfo.getUserId());
    }

    /**
     * 주소 수정
     */
    @PutMapping("/{addressId}")
    public AddressDto updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDto request,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        return addressSvc.modAddress(addressId, request, loginUserInfo.getUserId());
    }

    /**
     * 주소 삭제
     */
    @DeleteMapping("/{addressId}")
    public void deleteAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        addressSvc.delAddress(addressId, loginUserInfo.getUserId());
    }

    /**
     * 기본 주소 설정
     */
    @PatchMapping("/{addressId}/default")
    public void setDefaultAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {

        if (loginUserInfo == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        addressSvc.setDefaultAddress(addressId, loginUserInfo.getUserId());
    }
}
