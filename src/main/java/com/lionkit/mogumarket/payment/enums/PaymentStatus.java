package com.lionkit.mogumarket.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("READY", "결제 준비"),
    PAY_PENDING("PAY_PENDING", "결제 대기"),
    PAID("PAID", "결제 완료"),
    PARTIAL_CANCELLED("PARTIAL_CANCELLED", "결제 부분 취소"),
    CANCELLED("CANCELLED", "결제 전액 취소"),
    FAILED("FAILED", "결제 실패");


    private final String key;
    private final String title;
}