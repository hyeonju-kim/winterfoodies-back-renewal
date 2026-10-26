package com.winter.winter_back.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * description    :
 * fileName       : OrderRequest
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 * 26. 10. 26.       hj kim             주문 방식(orderType: 포장/배달) 추가 - 포장은 배송 주소 없이 주문 가능
 * 26. 10. 26.       hj kim             수량 상한 99, 가게 요청사항 100자 제한
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull(message = "매장 ID는 필수입니다")
    private Long storeId;

    @NotEmpty(message = "주문 항목은 필수입니다")
    private List<@Valid OrderItem> items;

    // 주문 방식 PICKUP(포장, 기본) / DELIVERY(배달)
    private String orderType;

    // 배달 주문일 때만 필수 (포장 주문은 비워도 된다)
    @Valid
    private DeliveryAddress deliveryAddress;

    // 포장 주문 연락처 (가게가 픽업 안내할 때 사용, 포장이면 서비스에서 필수·형식 검사)
    private String contactPhone;

    @NotBlank(message = "결제 방법은 필수입니다")
    private String paymentMethod;

    // 금액은 서버가 메뉴·옵션·매장 배달비로 다시 계산한다. 아래 두 값은 화면 표시 금액과 비교(불일치 로그)용으로만 받는다
    private BigDecimal totalAmount;

    private BigDecimal deliveryFee;

    @Size(max = 100, message = "가게 요청사항은 100자까지 입력할 수 있습니다")
    private String orderRequest;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        @NotNull(message = "메뉴 ID는 필수입니다")
        private Long menuId;

        @NotNull(message = "수량은 필수입니다")
        @Positive(message = "수량은 0보다 커야 합니다")
        @Max(value = 99, message = "수량은 99개까지 주문할 수 있습니다")
        private Integer quantity;

        private BigDecimal price; // 사용하지 않음 (서버가 메뉴 가격으로 계산)

        private List<OrderItemOption> options;
        private String specialRequest;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemOption {
        private Long optionId;
        private String value;
        private BigDecimal additionalPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAddress {
        @NotBlank(message = "배송 주소는 필수입니다")
        private String address;

        private String detailAddress;

        @NotBlank(message = "연락처는 필수입니다")
        private String phone;
    }
}