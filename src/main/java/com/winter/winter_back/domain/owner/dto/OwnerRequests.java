package com.winter.winter_back.domain.owner.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * description    : 사장님 API 요청 본문 모음 (영업 상태·대기시간·주문 단계·품절)
 * fileName       : OwnerRequests
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 * 26. 10. 26.       hj kim             메뉴 추가·수정, 가게 정보, 영업시간 요청 추가
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

    /** 메뉴 추가·수정 (카테고리는 이름으로, 없으면 새로 만든다) */
    @Data
    public static class MenuRequest {
        @NotBlank(message = "메뉴 이름을 입력해주세요")
        @Size(max = 50, message = "메뉴 이름은 50자까지 입력할 수 있습니다")
        private String name;

        @Size(max = 200, message = "메뉴 설명은 200자까지 입력할 수 있습니다")
        private String description;

        @NotNull(message = "가격을 입력해주세요")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
        @Max(value = 1000000, message = "가격은 100만원까지 입력할 수 있습니다")
        private Integer price;

        @Size(max = 30, message = "카테고리 이름은 30자까지 입력할 수 있습니다")
        private String categoryName;
    }

    /** 가게 정보 수정 (주소·위치는 지도 기능 때) */
    @Data
    public static class StoreInfoRequest {
        @NotBlank(message = "가게 이름을 입력해주세요")
        @Size(max = 50, message = "가게 이름은 50자까지 입력할 수 있습니다")
        private String name;

        @Size(max = 300, message = "가게 소개는 300자까지 입력할 수 있습니다")
        private String description;

        @Pattern(regexp = "^$|^0[0-9]{8,10}$", message = "전화번호는 숫자만 입력해주세요")
        private String phone;

        @NotNull(message = "최소 주문금액을 입력해주세요")
        @Min(value = 0, message = "최소 주문금액은 0원 이상이어야 합니다")
        @Max(value = 1000000, message = "최소 주문금액은 100만원까지 입력할 수 있습니다")
        private Integer minOrderAmount;
    }

    /** 영업시간 수정 (요일 7개를 한 번에) */
    @Data
    public static class HoursRequest {
        @NotNull(message = "영업시간을 입력해주세요")
        @Size(min = 1, max = 7, message = "요일은 1~7개까지 입력할 수 있습니다")
        private List<@Valid DayHours> days;
    }

    /** 요일 하나의 영업시간 */
    @Data
    public static class DayHours {
        @NotBlank(message = "요일을 입력해주세요")
        private String dayOfWeek;

        @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$", message = "시각은 HH:mm 형식으로 입력해주세요")
        private String openTime;

        @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$", message = "시각은 HH:mm 형식으로 입력해주세요")
        private String closeTime;

        @NotNull(message = "휴무 여부를 입력해주세요")
        private Boolean isHoliday;
    }

    /** 메뉴 품절 켜고 끄기 */
    @Data
    public static class SoldOutRequest {
        @NotNull(message = "품절 여부를 입력해주세요")
        private Boolean isSoldOut;
    }
}
