package com.winter.winter_back.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull(message = "매장 ID는 필수입니다")
    private Long storeId;

    @NotEmpty(message = "주문 항목은 필수입니다")
    private List<@Valid OrderItem> items;

    @NotNull(message = "배송 주소는 필수입니다")
    @Valid
    private DeliveryAddress deliveryAddress;

    @NotBlank(message = "결제 방법은 필수입니다")
    private String paymentMethod;

    @NotNull(message = "총 금액은 필수입니다")
    @Positive(message = "총 금액은 0보다 커야 합니다")
    private BigDecimal totalAmount;

    @NotNull(message = "배달비는 필수입니다")
    private BigDecimal deliveryFee;

    private String orderRequest;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        @NotNull(message = "메뉴 ID는 필수입니다")
        private Long menuId;

        @NotNull(message = "수량은 필수입니다")
        @Positive(message = "수량은 0보다 커야 합니다")
        private Integer quantity;

        @NotNull(message = "가격은 필수입니다")
        @Positive(message = "가격은 0보다 커야 합니다")
        private BigDecimal price;

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