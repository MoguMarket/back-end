package com.lionkit.mogumarket.purchase.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseStatus {

    /**
     * 공동구매 참여 후 대기 중인 상태 (결제 미완료)
     */
    WAITING("WAITING", "결제 대기"),

    /**
     * 공동구매 성사 후 결제 완료된 상태
     */
    PAID("PAID", "결제 완료"),

    /**
     * 사용자가 자발적으로 취소한 주문
     */
    CANCELLED_BY_USER("CANCELLED_BY_USER", "사용자 취소"),

    /**
     * 공동구매 실패 등 시스템 조건에 의한 자동 취소
     */
    CANCELLED_BY_SYSTEM("CANCELLED_BY_SYSTEM", "시스템 자동 취소"),

    /**
     * 결제 완료 후 환불까지 완료된 상태
     */
    REFUNDED("REFUNDED", "환불 완료"),

    /**
     * 결제 실패, 오류로 인한 실패 상태
     */
    FAILED("FAILED", "결제 실패");

    private final String key;
    private final String title;
}

