package com.lionkit.mogumarket.payment.adapter;


import com.lionkit.mogumarket.payment.enums.PaymentStatus;
import com.lionkit.mogumarket.payment.enums.PortonePaymentMethod;
import com.lionkit.mogumarket.payment.portone.enums.PortonePgVendor;
import lombok.Getter;

import java.time.Instant;


public interface PaymentGateway {

    /** merchantUid(= PortOne v2 paymentId) 기준 단건 조회 */
    PaymentLookupResult lookupByMerchantUid(String merchantUid);

    /** 결제(부분)취소 */
    CancelResult cancel(String paymentId, long cancelAmount, String reason);

    /** 웹훅 서명 검증 + 파싱 → 공통 이벤트로 반환(검증 실패시 예외) */
    WebhookEvent verifyAndParseWebhook(String rawBody, String msgId, String signature, String timestamp);

    /** 단순 서명 검증(Boolean) */
    boolean verifyWebhook(String rawBody, String msgId, String signature, String timestamp);


    /* ===== DTOs ===== */

    record PaymentLookupResult(
            String paymentId,
            String merchantUid,
            long totalAmount,
            String currency,
            PaymentStatus status,
            PortonePaymentMethod paymentMethod,
            String storeId,
            PortonePgVendor pgVendor,
            Instant requestedAt

    ) {}

    record CancelResult(
            String cancellationId,
            long cancelledAmount
    ) {}

    record WebhookEvent(
            String paymentId,
            String transactionId,
            PaymentStatus status,
            Long changedAmount,
            String cancellationId
    ) {}

    /* ===== 공통 예외 ===== */
    class PaymentGatewayException extends RuntimeException {
        public PaymentGatewayException(String msg, Throwable cause) { super(msg, cause); }
        public PaymentGatewayException(String msg) { super(msg); }
    }
}
