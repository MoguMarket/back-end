package com.lionkit.mogumarket.payment.portone.adapter;



import com.lionkit.mogumarket.payment.adapter.PaymentGateway;
import com.lionkit.mogumarket.payment.enums.PaymentStatus;
import com.lionkit.mogumarket.payment.enums.PortonePaymentMethod;
import com.lionkit.mogumarket.payment.portone.enums.PortonePgVendor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * PortOne SDK를 감싼 Facade(PortoneSdkFacade)를 다시 감싸
 * 서비스 레이어에 "결제 게이트웨이" 포트를 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class PortonePaymentAdapter implements PaymentGateway {

    private final PortoneSdkFacade sdk;

    @Override
    public PaymentLookupResult lookupByMerchantUid(String merchantUid) {
        try {
            var v = sdk.getPaymentByMerchantUid(merchantUid);
            return new PaymentLookupResult(
                v.paymentId(),
                    v.merchantUid(),
                    v.totalAmount(),
                    v.currency(),
                    mapStatus(v.status()),
                    v.paymentMethod(),
                    v.storeId(),
                    v.pgVendor(),
                    v.requestedAt()
            );
        } catch (Exception e) {
            throw new PaymentGatewayException("Portone lookup failed: " + merchantUid, e);
        }
    }

    @Override
    public CancelResult cancel(String paymentId, long cancelAmount, String reason) {
        try {
            var res = sdk.cancelPayment(paymentId, cancelAmount, reason);
            return new CancelResult(res.cancellationId(), res.cancelledAmount());
        } catch (Exception e) {
            throw new PaymentGatewayException("Portone cancel failed: " + paymentId, e);
        }
    }

    @Override
    public WebhookEvent verifyAndParseWebhook(String rawBody, String msgId, String signature, String timestamp) {
        try {
            var ev = sdk.parseWebhookToEvent(rawBody, msgId, signature, timestamp);
            return new WebhookEvent(
                    ev.paymentId(),
                    ev.transactionId(),
                    mapStatus(ev.eventType()),
                    ev.changedAmount(),
                    ev.cancellationId()
            );
        } catch (Exception e) {
            throw new PaymentGatewayException("Portone webhook verify/parse failed", e);
        }
    }

    @Override
    public boolean verifyWebhook(String rawBody, String msgId, String signature, String timestamp) {
        return sdk.verifyWebhook(rawBody, msgId, signature, timestamp);
    }

    /* =======================
       매핑/헬퍼
       ======================= */

    /** SDK/웹훅 status 문자열 → 우리 PaymentStatus */
    private static PaymentStatus mapStatus(String s) {
        if (s == null) return PaymentStatus.FAILED;
        return switch (s) {
            case "READY" -> PaymentStatus.READY;
            case "PAY_PENDING" -> PaymentStatus.PAY_PENDING;
            case "PAID" -> PaymentStatus.PAID;
            case "PARTIAL_CANCELLED" -> PaymentStatus.PARTIAL_CANCELLED;
            case "CANCELLED" -> PaymentStatus.CANCELLED;
            case "FAILED" -> PaymentStatus.FAILED;
            default -> PaymentStatus.FAILED; // UNRECOGNIZED 등 방어
        };
    }
}
