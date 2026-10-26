package com.winter.winter_back.domain.owner.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * description    : 사장님 API 요청 본문 모음 (영업 상태·대기시간·주문 단계·품절)
 * fileName       : OwnerRequests
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
public final class OwnerRequests {

    private OwnerRequests() {
    }

    /** 영업 상태 변경 (OPEN 영업중 / TEMPORARILY_CLOSED 잠시 쉼 / CLOSED 영업종료) */
    @Data
    public static class StoreStatusRequest {
        @NotBlank(message = "영업 상태를 선택해주세요")
        private String status;
    }

    /** 대기시간 변경 (0~120분) */
    @Data
    public static class WaitTimeRequest {
        @NotNull(message = "대기시간을 입력해주세요")
        @Min(value = 0, message = "대기시간은 0분 이상이어야 합니다")
        @Max(value = 120, message = "대기시간은 120분까지 설정할 수 있습니다")
        private Integer minutes;
    }

    /** 주문 단계 변경 (거절은 CANCELLED + 사유) */
    @Data
    public static class OrderStatusChangeRequest {
        @NotBlank(message = "바꿀 주문 상태를 입력해주세요")
        private String status;

        @Size(max = 200, message = "거절 사유는 200자까지 입력할 수 있습니다")
        private String reason;
    }

    /** 메뉴 품절 켜고 끄기 */
    @Data
    public static class SoldOutRequest {
        @NotNull(message = "품절 여부를 입력해주세요")
        private Boolean isSoldOut;
    }
}
