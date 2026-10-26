package com.winter.winter_back.domain.order.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * description    : 주문 상태
 * fileName       : OrderStatus
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 * 26. 10. 26.       hj kim             상수 클래스 → enum 으로 변경 (DB·응답에는 name() 문자열로 저장)
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("주문 접수 대기중", "주문 접수 대기중"),
    CONFIRMED("주문 확인됨", "30분 - 40분"),
    PREPARING("음식 준비중", "20분 - 30분"),
    DELIVERING("배달중", "10분 - 15분"),
    DELIVERED("배달 완료", "배달 완료"),
    CANCELLED("주문 취소됨", "주문 취소됨");

    private final String description;    // 상태 설명
    private final String estimatedTime;  // 상태별 예상 소요 시간 안내

    /**
     * DB 에 저장된 상태 문자열을 enum 으로 변환 (알 수 없는 값이면 null)
     */
    public static OrderStatus from(String status) {
        for (OrderStatus orderStatus : values()) {
            if (orderStatus.name().equals(status)) {
                return orderStatus;
            }
        }
        return null;
    }

    /**
     * 주문 취소가 가능한 상태인지 (접수 대기·주문 확인 단계만 취소 가능)
     */
    public boolean canCancel() {
        return this == PENDING || this == CONFIRMED;
    }
}
