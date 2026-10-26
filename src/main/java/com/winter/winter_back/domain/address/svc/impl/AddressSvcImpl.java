package com.winter.winter_back.domain.address.svc.impl;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.address.dao.AddressDao;
import com.winter.winter_back.domain.address.dto.AddressDto;
import com.winter.winter_back.domain.address.dto.AddressListResponse;
import com.winter.winter_back.domain.address.svc.AddressSvc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * description    : 주소 관리 서비스 구현체
 * fileName       : AddressSvcImpl
 * author         : hj kim
 * date           : 25. 8. 30.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 30.        hj kim             최초 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressSvcImpl implements AddressSvc {

    private final AddressDao addressDao;

    private static final int MAX_ADDRESS_COUNT = 10; // 최대 주소 개수 제한

    @Override
    public AddressListResponse getAddressList(String userId) {
        log.info("주소 목록 조회 요청: {}", userId);

        List<AddressDto> addressList = addressDao.getAddressList(userId);

        return AddressListResponse.builder()
                .addresses(addressList)
                .totalCount(addressList.size())
                .build();
    }

    @Override
    public AddressDto getAddress(Long addressId, String userId) {
        log.info("주소 상세 조회 요청: addressId={}, userId={}", addressId, userId);

        AddressDto address = addressDao.getAddress(addressId, userId);

        if (address == null) {
            throw new BizException("address_not_found");
        }

        return address;
    }

    @Override
    @Transactional
    public AddressDto regAddress(AddressDto request, String userId) {
        log.info("주소 등록 요청: userId={}, alias={}", userId, request.getAlias());

        // 1. 주소 개수 제한 확인
        int currentCount = addressDao.getAddressCount(userId);
        if (currentCount >= MAX_ADDRESS_COUNT) {
            throw new BizException("address_limit_exceeded", String.valueOf(MAX_ADDRESS_COUNT));
        }

        // 2. 기본 주소 설정 처리
        if ("Y".equals(request.getIsDefault())) {
            // 기존 기본 주소 해제
            addressDao.modAllAddressDefaultN(userId);
        } else if (currentCount == 0) {
            // 첫 번째 주소는 자동으로 기본 주소로 설정
            request.setIsDefault("Y");
        }

        // 3. 사용자 ID 설정
        request.setUserId(userId);

        // 4. 주소 등록
        int result = addressDao.regAddress(request);
        if (result == 0) {
            throw new BizException("fail_add");
        }

        log.info("주소 등록 완료: idx={}", request.getIdx());
        return request;
    }

    @Override
    @Transactional
    public AddressDto modAddress(Long addressId, AddressDto request, String userId) {
        log.info("주소 수정 요청: addressId={}, userId={}", addressId, userId);

        // 1. 기존 주소 존재 확인
        AddressDto existingAddress = addressDao.getAddress(addressId, userId);
        if (existingAddress == null) {
            throw new BizException("address_not_found");
        }

        // 2. 수정할 데이터 설정
        request.setIdx(addressId);
        request.setUserId(userId);

        // 3. 주소 수정
        int result = addressDao.modAddress(request);
        if (result == 0) {
            throw new BizException("fail_update");
        }

        log.info("주소 수정 완료: idx={}", addressId);
        return addressDao.getAddress(addressId, userId);
    }

    @Override
    @Transactional
    public void delAddress(Long addressId, String userId) {
        log.info("주소 삭제 요청: addressId={}, userId={}", addressId, userId);

        // 1. 기존 주소 존재 확인
        AddressDto existingAddress = addressDao.getAddress(addressId, userId);
        if (existingAddress == null) {
            throw new BizException("address_not_found");
        }

        // 2. 기본 주소 삭제 시 다른 주소를 기본으로 설정
        boolean wasDefault = "Y".equals(existingAddress.getIsDefault());

        // 3. 주소 삭제
        int result = addressDao.delAddress(addressId, userId);
        if (result == 0) {
            throw new BizException("fail_remove");
        }

        // 4. 삭제된 주소가 기본 주소였다면 다른 주소를 기본으로 설정
        if (wasDefault) {
            List<AddressDto> remainingAddresses = addressDao.getAddressList(userId);
            if (!remainingAddresses.isEmpty()) {
                // 가장 최근에 생성된 주소를 기본 주소로 설정
                Long newDefaultAddressId = remainingAddresses.get(0).getIdx();
                addressDao.modAddressDefaultY(newDefaultAddressId, userId);
                log.info("새로운 기본 주소 설정: idx={}", newDefaultAddressId);
            }
        }

        log.info("주소 삭제 완료: idx={}", addressId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long addressId, String userId) {
        log.info("기본 주소 설정 요청: addressId={}, userId={}", addressId, userId);

        // 1. 주소 존재 확인
        AddressDto address = addressDao.getAddress(addressId, userId);
        if (address == null) {
            throw new BizException("address_not_found");
        }

        // 2. 이미 기본 주소인 경우
        if ("Y".equals(address.getIsDefault())) {
            log.info("이미 기본 주소로 설정되어 있습니다: idx={}", addressId);
            return;
        }

        // 3. 기존 기본 주소 해제
        addressDao.modAllAddressDefaultN(userId);

        // 4. 새로운 기본 주소 설정
        int result = addressDao.modAddressDefaultY(addressId, userId);
        if (result == 0) {
            throw new BizException("fail_update");
        }

        log.info("기본 주소 설정 완료: idx={}", addressId);
    }
}