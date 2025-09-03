package com.winter.winter_back.domain.order.dto;

/**
 * description    : 주문 상수 클래스
 * fileName       : OrderStatus
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */
public class OrderStatus {
    public static final String PENDING = "PENDING";           // 주문 접수 대기
    public static final String CONFIRMED = "CONFIRMED";       // 주문 확인됨
    public static final String PREPARING = "PREPARING";       // 음식 준비중
    public static final String DELIVERING = "DELIVERING";     // 배달중
    public static final String DELIVERED = "DELIVERED";       // 배달 완료
    public static final String CANCELLED = "CANCELLED";       // 주문 취소됨

    public static String getStatusDescription(String status) {
        switch (status) {
            case PENDING: return "주문 접수 대기중";
            case CONFIRMED: return "주문 확인됨";
            case PREPARING: return "음식 준비중";
            case DELIVERING: return "배달중";
            case DELIVERED: return "배달 완료";
            case CANCELLED: return "주문 취소됨";
            default: return "확인 중";
        }
    }
}
