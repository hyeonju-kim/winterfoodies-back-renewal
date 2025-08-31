package com.winter.winter_back.domain.address.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * description    :
 * fileName       : AddressListResponse
 * author         : hyeonju
 * date           : 25. 8. 30.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 30.        hyeonju             최초 생성
 */
@Data
@Builder
public class AddressListResponse {
    private List<AddressDto> addresses;
    private int totalCount;
    private AddressDto defaultAddress; // 기본 주소 따로 표시
}
