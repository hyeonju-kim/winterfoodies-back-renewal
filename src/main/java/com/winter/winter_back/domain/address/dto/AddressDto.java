package com.winter.winter_back.domain.address.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * description    :
 * fileName       : AddressDto
 * author         : hyeonju
 * date           : 25. 8. 30.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 30.        hyeonju             최초 생성
 */
@Data
public class AddressDto {
    private Long idx;

    @NotBlank(message = "사용자 ID는 필수입니다")
    private String userId;

    @NotBlank(message = "주소 별칭은 필수입니다")
    @Size(max = 20, message = "주소 별칭은 20자 이내여야 합니다")
    private String alias; // 집, 회사, 기타 등

    @NotBlank(message = "기본 주소는 필수입니다")
    private String baseAddress; // 도로명주소 또는 지번주소

    private String detailAddress; // 상세주소 (동, 호수 등)

    private String fullAddress; // baseAddress + detailAddress 조합

    private Double latitude; // 위도

    private Double longitude; // 경도

    private String isDefault; // 기본 주소 여부 (Y/N)

    private String deliveryInstructions; // 배달 요청사항

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // 전체 주소 조합 메서드
    public String getFullAddress() {
        if (detailAddress == null || detailAddress.trim().isEmpty()) {
            return baseAddress;
        }
        return baseAddress + " " + detailAddress;
    }
}
