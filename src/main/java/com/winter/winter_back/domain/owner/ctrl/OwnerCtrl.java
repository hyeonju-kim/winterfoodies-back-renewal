package com.winter.winter_back.domain.owner.ctrl;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.common.security.dto.ComLoginUserInfoDto;
import com.winter.winter_back.domain.owner.dto.OwnerHoursResponse;
import com.winter.winter_back.domain.owner.dto.OwnerMenuResponse;
import com.winter.winter_back.domain.owner.dto.OwnerOrderResponse;
import com.winter.winter_back.domain.owner.dto.OwnerRequests;
import com.winter.winter_back.domain.owner.dto.OwnerStoreResponse;
import com.winter.winter_back.domain.owner.svc.OwnerSvc;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description    : 사장님 컨트롤러 (/owner/** 는 ROLE_OWNER 만 - SecurityConfig)
 * fileName       : OwnerCtrl
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 * 26. 10. 26.       hj kim             메뉴 추가·수정·삭제, 가게 정보 수정, 영업시간 조회·수정 추가
 */
@Slf4j
@RestController
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerCtrl {

    private final OwnerSvc ownerSvc;

    /**
     * 내 가게 + 오늘 현황
     */
    @GetMapping("/store")
    public OwnerStoreResponse getMyStore(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {
        return ownerSvc.getMyStore(loginUserInfo.getUserId());
    }

    /**
     * 영업 상태 변경
     */
    @PatchMapping("/store/status")
    public OwnerStoreResponse modStoreStatus(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
                                             @RequestBody @Valid OwnerRequests.StoreStatusRequest request) {
        return ownerSvc.modStoreStatus(loginUserInfo.getUserId(), request);
    }

    /**
     * 대기시간 변경
     */
    @PatchMapping("/store/wait-time")
    public OwnerStoreResponse modWaitTime(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
                                          @RequestBody @Valid OwnerRequests.WaitTimeRequest request) {
        return ownerSvc.modWaitTime(loginUserInfo.getUserId(), request);
    }

    /**
     * 내 가게 주문 목록 (status 를 여러 개 주면 그중 하나인 주문, 없으면 전체)
     */
    @GetMapping("/orders")
    public ListResponseDto<OwnerOrderResponse> getOrderList(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
                                                            @RequestParam(required = false) List<String> status,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int limit) {
        return ownerSvc.getOrderList(loginUserInfo.getUserId(), status, page, limit);
    }

    /**
     * 주문 단계 변경 (수락·조리 시작·픽업 준비 완료·픽업 완료·거절)
     */
    @PatchMapping("/orders/{orderId}/status")
    public OwnerOrderResponse modOrderStatus(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
                                             @PathVariable String orderId,
                                             @RequestBody @Valid OwnerRequests.OrderStatusChangeRequest request) {
        return ownerSvc.modOrderStatus(loginUserInfo.getUserId(), orderId, request);
    }

    /**
     * 내 가게 메뉴 목록
     */
    @GetMapping("/menus")
    public List<OwnerMenuResponse> getMenuList(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {
        return ownerSvc.getMenuList(loginUserInfo.getUserId());
    }

    /**
     * 메뉴 추가
     */
    @PostMapping("/menus")
    public OwnerMenuResponse regMenu(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
                                     @RequestBody @Valid OwnerRequests.MenuRequest request) {
        return ownerSvc.regMenu(loginUserInfo.getUserId(), request);
    }

    /**
     * 메뉴 수정
     */
    @PutMapping("/menus/{menuId}")
    public OwnerMenuResponse modMenu(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
                                     @PathVariable Long menuId,
                                     @RequestBody @Valid OwnerRequests.MenuRequest request) {
        return ownerSvc.modMenu(loginUserInfo.getUserId(), menuId, request);
    }

    /**
     * 메뉴 삭제
     */
    @DeleteMapping("/menus/{menuId}")
    public void delMenu(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo, @PathVariable Long menuId) {
        ownerSvc.delMenu(loginUserInfo.getUserId(), menuId);
    }

    /**
     * 가게 정보 수정 (이름·소개·전화·최소 주문금액)
     */
    @PutMapping("/store")
    public OwnerStoreResponse modStoreInfo(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
                                           @RequestBody @Valid OwnerRequests.StoreInfoRequest request) {
        return ownerSvc.modStoreInfo(loginUserInfo.getUserId(), request);
    }

    /**
     * 영업시간 조회 (월~일)
     */
    @GetMapping("/store/hours")
    public List<OwnerHoursResponse> getHours(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo) {
        return ownerSvc.getHours(loginUserInfo.getUserId());
    }

    /**
     * 영업시간 수정
     */
    @PutMapping("/store/hours")
    public List<OwnerHoursResponse> modHours(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
                                             @RequestBody @Valid OwnerRequests.HoursRequest request) {
        return ownerSvc.modHours(loginUserInfo.getUserId(), request);
    }

    /**
     * 메뉴 품절 켜고 끄기
     */
    @PatchMapping("/menus/{menuId}/sold-out")
    public void modMenuSoldOut(@AuthenticationPrincipal ComLoginUserInfoDto loginUserInfo,
                               @PathVariable Long menuId,
                               @RequestBody @Valid OwnerRequests.SoldOutRequest request) {
        ownerSvc.modMenuSoldOut(loginUserInfo.getUserId(), menuId, request);
    }
}
