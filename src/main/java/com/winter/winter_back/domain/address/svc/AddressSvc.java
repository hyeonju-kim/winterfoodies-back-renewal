package com.winter.winter_back.domain.address.svc;

import com.winter.winter_back.domain.address.dto.AddressDto;
import com.winter.winter_back.domain.address.dto.AddressListResponse;

/**
 * description    : 주소 서비스 인터페이스
 * fileName       : AddressSvc
 * author         : hj kim
 * date           : 25. 8. 30.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 30.        hj kim             최초 생성
 */
public interface AddressSvc {
    /**
     * 사용자 주소 목록 조회
     * @param userId 사용자 ID
     * @return 주소 목록
     */
    AddressListResponse getAddressList(String userId);

    /**
     * 주소 상세 조회
     * @param addressId 주소 ID
     * @param userId 사용자 ID
     * @return 주소 상세정보
     */
    AddressDto getAddress(Long addressId, String userId);

    /**
     * 주소 등록
     * @param request 주소 등록 요청
     * @param userId 사용자 ID
     * @return 등록된 주소 정보
     */
    AddressDto regAddress(AddressDto request, String userId);

    /**
     * 주소 수정
     * @param addressId 주소 ID
     * @param request 주소 수정 요청
     * @param userId 사용자 ID
     * @return 수정된 주소 정보
     */
    AddressDto modAddress(Long addressId, AddressDto request, String userId);

    /**
     * 주소 삭제
     * @param addressId 주소 ID
     * @param userId 사용자 ID
     */
    void delAddress(Long addressId, String userId);

    /**
     * 기본 주소 설정
     * @param addressId 주소 ID
     * @param userId 사용자 ID
     */
    void setDefaultAddress(Long addressId, String userId);
}
