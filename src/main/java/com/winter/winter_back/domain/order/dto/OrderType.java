package com.winter.winter_back.domain.order.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * description    : 주문 방식 (포장 / 배달)
 * fileName       : OrderType
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성 (포장 전용 서비스로 전환, 배달은 추후 확장용)
 */
@Getter
@RequiredArgsConstructor
public enum OrderType {
    PICKUP("포장"),
    DELIVERY("배달");

    private final String description;

    /**
     * 요청 문자열을 enum 으로 변환 (값이 없으면 포장, 알 수 없는 값이면 null)
     */
    public static OrderType from(String orderType) {
        if (orderType == null || orderType.isBlank()) {
            return PICKUP;
        }
        for (OrderType type : values()) {
            if (type.name().equals(orderType)) {
                return type;
            }
        }
        return null;
    }
}
