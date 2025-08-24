package com.lionkit.mogumarket.global.base.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionType {
    // common
    UNEXPECTED_SERVER_ERROR(INTERNAL_SERVER_ERROR, "C001", "예상치못한 서버에러 발생"),
    BINDING_ERROR(BAD_REQUEST, "C002", "바인딩시 에러 발생"),
    ESSENTIAL_FIELD_MISSING_ERROR(NO_CONTENT , "C003","필수적인 필드 부재"),
    INVALID_VALUE_ERROR(NOT_ACCEPTABLE , "C004","값이 유효하지 않음"),
    DUPLICATE_VALUE_ERROR(NOT_ACCEPTABLE , "C005","값이 중복됨"),

    // auth
    INVALID_REFRESH_TOKEN(NOT_ACCEPTABLE , "A001","유효하지 않은 리프레시 토큰"),
    REFRESH_TOKEN_EXPIRED(UNAUTHORIZED,"A002","리프레시 토큰 만료"),
    PASSWORD_NOT_MATCHED(NOT_ACCEPTABLE , "A003","비밀번호 불일치"),

    // oauth2
    INVALID_PROVIDER_TYPE_ERROR(NOT_ACCEPTABLE , "O001","지원하지 않는 provider"),

    // user
    USER_NOT_FOUND(NOT_FOUND, "U001", "존재하지 않는 사용자"),
    DUPLICATED_USER_ID(CONFLICT, "U002", "중복 아이디(PK)"),
    DUPLICATED_USERNAME(CONFLICT, "U003", "중복 아이디(username)"),
    ALREADY_REGISTERED_USER(NOT_ACCEPTABLE , "U006","이미 최종 회원 가입된 사용자"),
    NOT_REGISTERED_USER(FORBIDDEN , "U007","최종 회원 가입 되지 않은 사용자"),
    UNAUTHORIZED_USER(UNAUTHORIZED, "U005","로그인 되지 않은 사용자"),


    //cart
    INVALID_QTYBASE(NOT_ACCEPTABLE , "C001","카트 적재 수량은 1 이상이어야 합니다."),
    CART_NOT_FOUND(NOT_FOUND, "C002", "유저의 카트가 존재하지 않습니다"),

    //point
    POINT_WALLET_NOT_FOUND(NOT_FOUND, "P001", "해당 사용자에 대한 point wallet 을 찾을 수 없습니다."),

    /**
     * 포인트 차감 "시도" 시점에 보유중인 포인트가 부족할 경우 발생하는 exception
     */
    POINT_INSUFFICIENT_AVAILABLE(NOT_ACCEPTABLE,"P002","보유중인 포인트보다 더 많은 포인트 차감 시도는 불가합니다."),

    /**
     * hold 된 포인트에 대한 작업을 시도하는 상황에서,
     * hold 된 포인트량보다 더 큰 포인트량에 대해 작업을 시도할 경우 발생하는 exception
     */
    POINT_HOLD_INSUFFICIENT(INTERNAL_SERVER_ERROR,"P003","실제로 hold 된 포인트 보다 더 많은 hold 포인트에 대한 작업 시도 발생"),

    /**
     * "최종" 포인트 차감 시점에 보유중인 포인트가 부족할 경우 발생하는 exception
     */
    POINT_BALANCE_INSUFFICIENT(NOT_ACCEPTABLE,"P004","보유중인 포인트가 부족합니다."),


    //group buy
    GROUP_BUY_NOT_FOUND(BAD_REQUEST,"G001","존재하지 않는 공동구매."),
    GROUP_BUY_NOT_OPEN(BAD_REQUEST,"G002","진행중이지 않은 공동구매."),

    //groupbuy stage
    GROUP_BUY_STAGE_NOT_FOUND(BAD_REQUEST,"GS001","존재하지 않는 공동구매 단계"),
    GROUPBUY_ALREADY_OPEN(BAD_REQUEST,"GS002","해당 상품에 대한 공동구매가 이미 개설되어있습니다"),
    //order
    ORDER_NOT_FOUND(BAD_REQUEST,"O001","존재하지 않는 order(주문건)"),
    INVALID_ORDER_STATUS(NOT_ACCEPTABLE,"O002","유효하지 않은 order status"),
    INVALID_ORDER_AMOUNT(NOT_ACCEPTABLE , "O003","유효하지 않은 order amount"),

    //orderline
    ORDER_LINE_NOT_FOUND(BAD_REQUEST,"OL001","존재하지 않는 order line(상품 주문)"),

    //store
    STORE_NOT_FOUND(NOT_FOUND, "S001", "존재하지 않는 가게"),

    // product
    PRODUCT_NOT_FOUND(NOT_FOUND, "P001", "존재하지 않는 상품"),
    STOCK_OVERFLOW(NOT_ACCEPTABLE, "P002", "해당 상품의 재고 초과"),
    INVALID_QTY(NOT_ACCEPTABLE,"P003","구매량은 양수여야 합니다"),
    PRODUCT_LOCK_TIMEOUT(LOCKED,"P004","구매에 대한 비관적 락 대기 초과, 재시도 바람."),
    PRODUCT_LOCK_CONFLICT(CONFLICT,"P005","구매에 대한 비관적 락 관련 오류"),
    STAGE_NOT_DEFINED(BAD_REQUEST,"P006","상품의 단계가 설정되지 않음"),

    //payment
    PAYMENT_NOT_FOUND(NOT_FOUND, "P001", "존재하지 결재 정보"),
    INVALID_AMOUNT(NOT_ACCEPTABLE, "P002", "유효하지 않은 금액"),

    //REFUND
    REFUND_EXCEEDS_AVAILABLE(NOT_ACCEPTABLE, "C001", "환불 가능액보다 더 큰 금액의 환불은 불가합니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;}
