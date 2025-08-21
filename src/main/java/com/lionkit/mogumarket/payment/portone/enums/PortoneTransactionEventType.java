package com.lionkit.mogumarket.payment.portone.enums;

import com.lionkit.mogumarket.payment.enums.PaymentStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PortoneTransactionEventType {
    TRANSACTION_READY("TRANSACTION_READY", "Transaction.Ready"),
    TRANSACTION_PAY_PENDING("TRANSACTION_PAY_PENDING", "Transaction.PayPending"),
    TRANSACTION_PAID("TRANSACTION_PAID", "Transaction.Paid"),
    TRANSACTION_PARTIAL_CANCELLED("TRANSACTION_PARTIAL_CANCELLED", "Transaction.PartialCancelled"),
    TRANSACTION_CANCELLED("TRANSACTION_CANCELLED", "Transaction.Cancelled"),
    TRANSACTION_FAILED("TRANSACTION_FAILED", "Transaction.Failed");

    private final String key;

    /** 포트원 원문 이벤트명(표시/매핑용) */
    private final String title;

    /** 역매핑: 내부 키 → enum */
    public static PortoneTransactionEventType fromKey(String key) {
        for (var e : values()) if (e.key.equals(key)) return e;
        throw new IllegalArgumentException("Unknown key: " + key);
    }

    /** 역매핑: 포트원 원문 이벤트명 → enum */
    public static PortoneTransactionEventType fromTitle(String title) {
        for (var e : values()) if (e.title.equals(title)) return e;
        throw new IllegalArgumentException("Unknown title: " + title);
    }

    /** (호환) 기존 wireName 기반 사용처가 있다면 그대로 호출해도 됩니다. */
    public static PortoneTransactionEventType fromWireName(String wireName) {
        return fromTitle(wireName);
    }

    /** 우리 공통 PaymentStatus로의 편의 매핑 */
    public PaymentStatus toPaymentStatus() {
        return switch (this) {
            case TRANSACTION_READY -> PaymentStatus.READY;
            case TRANSACTION_PAY_PENDING -> PaymentStatus.PAY_PENDING;
            case TRANSACTION_PAID -> PaymentStatus.PAID;
            case TRANSACTION_PARTIAL_CANCELLED -> PaymentStatus.PARTIAL_CANCELLED;
            case TRANSACTION_CANCELLED -> PaymentStatus.CANCELLED;
            case TRANSACTION_FAILED -> PaymentStatus.FAILED;
        };
    }
}