package com.winter.winter_back.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * description    :
 * fileName       : ErrorCode
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.         hj kim             최초 생성
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C000", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "엔티티를 찾을 수 없습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근이 거부되었습니다."),

    // Authentication & Authorization
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "만료된 토큰입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U003", "잘못된 비밀번호입니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "매장을 찾을 수 없습니다."),
    STORE_CLOSED(HttpStatus.BAD_REQUEST, "S002", "매장이 영업 중이 아닙니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S003", "매장에 접근할 수 없습니다."),

    // Menu
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "메뉴를 찾을 수 없습니다."),
    MENU_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "메뉴 옵션을 찾을 수 없습니다."),
    MENU_UNAVAILABLE(HttpStatus.BAD_REQUEST, "M003", "주문할 수 없는 메뉴입니다."),

    // Cart
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CT002", "장바구니 아이템을 찾을 수 없습니다."),
    CART_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CT003", "장바구니에 접근할 수 없습니다."),
    DIFFERENT_STORE_ITEMS(HttpStatus.BAD_REQUEST, "CT004", "다른 매장의 메뉴는 함께 주문할 수 없습니다."),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "CT005", "장바구니가 비어있습니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "CT006", "유효하지 않은 수량입니다."),
    MIN_ORDER_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST, "CT007", "최소 주문 금액을 충족하지 않습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "O002", "주문에 접근할 수 없습니다."),
    ORDER_CANNOT_BE_CANCELLED(HttpStatus.BAD_REQUEST, "O003", "취소할 수 없는 주문입니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "O004", "유효하지 않은 주문 상태입니다."),

    // Payment
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "P001", "결제에 실패했습니다."),
    PAYMENT_CANCELLED(HttpStatus.BAD_REQUEST, "P002", "결제가 취소되었습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "P003", "유효하지 않은 결제 금액입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}