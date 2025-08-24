package com.lionkit.mogumarket.payment.portone.adapter;


import com.lionkit.mogumarket.payment.enums.PortonePaymentMethod;
import com.lionkit.mogumarket.payment.portone.enums.PortonePgVendor;
import io.portone.sdk.server.PortOneClient;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.payment.*;
import io.portone.sdk.server.webhook.*;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * PortOne JVM Server SDK(0.19.x) 얇은 파사드.
 * - 외부에서는 PortOne SDK 세부 타입을 몰라도 되도록 단순 VO 반환
 * - SDK 업그레이드 시 이 클래스만 수정
 *
 * 사용한 SDK 주요 규칙:
 *  - 모든 API는 CompletableFuture 반환 → 동기 흐름에서는 .get(...)으로 변환
 *  - Payment는 sealed interface.
 *      * 금액/통화 등은 Payment.Recognized에서만 접근 가능
 *      * 상태(Status)는 별도 필드가 아니라 런타임 서브타입으로 표현
 */
@Component
@RequiredArgsConstructor
public class PortoneSdkFacade {

    private static final long DEFAULT_TIMEOUT_SEC = 10; // 네트워크 안전망(필요에 따라 조정)

    private final PortOneClient portOneClient;  // PortOne SDK Root
    private final WebhookVerifier webhook;      // Webhook 서명 검증기

    /**
     * SDK의 Payment 클라이언트 획득 (매 호출 시 동일 인스턴스 반환)
     */
    private PaymentClient paymentClient() {
        return portOneClient.getPayment();
    }

    /**
     * 결제 단건 조회(PortOne paymentId 기준).
     *
     * @param paymentId PortOne 결제 식별자 (v2에서 기존 merchantUid와 동일 개념)
     * @return PaymentView (총 결제요청 금액/통화/상태 등)
     * @throws Exception 네트워크/역직렬화/SDK 예외
     */
    public PaymentView getPaymentById(String paymentId) throws Exception {
        var p = paymentClient()
                .getPayment(Objects.requireNonNull(paymentId, "paymentId must not be null"))
                .get(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS);
        return toView(p);
    }

    /**
     * 결제 단건 조회(merchantUid 명칭으로 들어오더라도 v2에선 paymentId와 동일하게 처리).
     *
     * @param merchantUid v2에선 사실상 PortOne paymentId와 동일
     * @return PaymentView
     * @throws Exception 네트워크/역직렬화/SDK 예외
     */
    public PaymentView getPaymentByMerchantUid(String merchantUid) throws Exception {
        var p = paymentClient()
                .getPayment(Objects.requireNonNull(merchantUid, "merchantUid must not be null"))
                .get(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS);
        return toView(p);
    }

    /**
     * (부분)취소 수행.
     *
     * @param paymentId    PortOne paymentId
     * @param cancelAmount 취소 금액(원). 전액취소 시 총 결제요청 금액과 동일하게 전달
     * @param reason       취소 사유(필수)
     * @return 취소 식별자/취소금액을 담은 CancellationView
     * @throws Exception 네트워크/역직렬화/SDK 예외
     *                   <p>
     *                   주의:
     *                   - 계좌/가상계좌 등 일부 채널은 환불계좌 정보가 필요합니다.
     *                   - requester는 Admin/Customer 중 하나를 사용합니다(아래 기본값은 Admin).
     *                   - 금액 항목(taxFreeAmount, vatAmount 등)이 필요한 케이스가 아니면 null로 둡니다.
     */
    public CancellationView cancelPayment(String paymentId, long cancelAmount, String reason) throws Exception {
        if (cancelAmount <= 0) throw new IllegalArgumentException("cancelAmount must be positive");
        Objects.requireNonNull(reason, "reason must not be null");

        // 환불계좌가 필요한 채널에서만 채우면됨
        @Nullable CancelPaymentBodyRefundAccount refund = null;


        var res = paymentClient()
                .cancelPayment(
                        Objects.requireNonNull(paymentId, "paymentId must not be null"),
                        cancelAmount,                    // Long amount (오토박싱)
                        null,                            // taxFreeAmount (면세액 있으면 지정)
                        null,                            // vatAmount (부가세 분리 지정 시)
                        reason,                          // 취소 사유
                        CancelRequester.Admin.INSTANCE,  // 또는 Customer.INSTANCE
                        null,                            // PromotionDiscountRetainOption
                        null,                            // currentCancellableAmount(경합 보호용, 알면 넣기)
                        refund                           // 환불 계좌 정보(필요 채널)
                )
                .get(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS);

        var c = res.getCancellation();
        String cancellationId = null;
        long cancelled = 0L;
        if (c instanceof PaymentCancellation.Recognized rc) {
            cancellationId = rc.getId();
            cancelled = rc.getTotalAmount(); // primitive long
        }
        return new CancellationView(cancellationId, cancelled);
    }

    /**
     * 웹훅 서명 검증 + 파싱.
     *
     * @param rawBody         요청 본문 원문(UTF-8). 가공 금지
     * @param msgIdHeader     X-PORTONE-WEBHOOK-ID 등 ID 헤더(환경에 따라 키명이 다를 수 있음)
     * @param signatureHeader X-PORTONE-WEBHOOK-SIGNATURE 등 서명 헤더
     * @param timestampHeader X-PORTONE-WEBHOOK-TIMESTAMP 등 타임스탬프 헤더
     * @return 파싱된 Webhook 객체 (타입 패턴 매칭으로 이벤트 분기)
     * @throws WebhookVerificationException 서명 검증 실패 시
     */
    public Webhook verifyAndParseWebhook(
            String rawBody,
            @Nullable String msgIdHeader,
            @Nullable String signatureHeader,
            @Nullable String timestampHeader
    ) throws WebhookVerificationException {
        return webhook.verify(
                Objects.requireNonNull(rawBody, "rawBody must not be null"),
                msgIdHeader,
                signatureHeader,
                timestampHeader
        );
    }

    /**
     * 웹훅 서명 검증 (boolean 버전).
     * 실패 시 예외를 삼키고 false 반환합니다.
     */
    public boolean verifyWebhook(
            String rawBody,
            @Nullable String msgIdHeader,
            @Nullable String signatureHeader,
            @Nullable String timestampHeader
    ) {
        try {
            verifyAndParseWebhook(rawBody, msgIdHeader, signatureHeader, timestampHeader);
            return true;
        } catch (WebhookVerificationException e) {
            return false;
        }
    }

    /* =========================
       내부 변환/헬퍼
       ========================= */

    /**
     * SDK Payment → 내부 View 로 변환. Unrecognized 는 안전한 기본값으로 매핑
     */
    private static PaymentView toView(Payment payment) {
        if (payment instanceof Payment.Recognized p) {
            long total = (p.getAmount() != null) ? p.getAmount().getTotal() : 0L; // getTotal()은 long
            String currency = String.valueOf(p.getCurrency()); // enum/값 객체 모두 안전
            String status = inferStatus(payment);


            // v2에선 merchantUid == paymentId 로 취급
            return new PaymentView(
                    p.getId(),
                    p.getId(),
                    total,
                    currency,
                    status,
                    PortonePaymentMethod.CARD,
                    p.getStoreId(),
                    PortonePgVendor.KG_INICIS,
                    p.getRequestedAt()

            );
        }
        return PaymentView.builder().status("UNRECOGNIZED").build();
    }

    /**
     * Payment 상태는 런타임 서브타입으로 구분됨
     */
    private static String inferStatus(Payment payment) {
        if (payment instanceof ReadyPayment) return "READY";
        if (payment instanceof PayPendingPayment) return "PAY_PENDING";
        if (payment instanceof PaidPayment) return "PAID";
        if (payment instanceof CancelledPayment) return "CANCELLED"; // 부분취소 여부는 별도 이벤트/필드로 판단
        if (payment instanceof FailedPayment) return "FAILED";
        return "UNRECOGNIZED";
    }

    /* =========================
       외부에 노출할 단순 View
       ========================= */

    /**
     * 총 결제요청 금액(부분취소 전 기준)을 담은 스냅샷 View
     */

    @Builder
    public record PaymentView(
            String paymentId,
            String merchantUid,
            long totalAmount,
            String currency,
            String status,
            PortonePaymentMethod paymentMethod,
            String storeId,
            PortonePgVendor pgVendor,
            Instant requestedAt

    ) {
    }

    /**
     * 취소 결과 View
     */
    public record CancellationView(
            String cancellationId,
            long cancelledAmount) {
    }



    /**
     * 웹훅 본문/헤더 검증 후, 우리 서비스가 쓰기 쉬운 공통 이벤트 DTO로 매핑.
     * - 검증 실패시 WebhookVerificationException 발생
     * - 이벤트 타입은 포트원의 트랜잭션 서브타입으로 판별
     */
    public PortoneEvent parseWebhookToEvent(
            String rawBody,
            String msgIdHeader,
            String signatureHeader,
            String timestampHeader
    ) throws WebhookVerificationException {
        Webhook w = verifyAndParseWebhook(rawBody, msgIdHeader, signatureHeader, timestampHeader);

        // 트랜잭션류 이벤트만 잡아 간결 DTO로 리턴.
        // (다른 웹훅 타입이 있다면 필요할 때 case 추가)
        if (w instanceof WebhookTransaction tx) {
            var d = tx.getData();
            String paymentId     = (d != null) ? d.getPaymentId()     : null;
            String transactionId = (d != null) ? d.getTransactionId() : null;
            String eventType     = inferWebhookEventType(tx);
            Long changedAmount   = extractWebhookChangedAmount(tx);
            String cancellationId= extractWebhookCancellationId(tx);

            return new PortoneEvent(paymentId, transactionId, eventType, changedAmount, cancellationId);
        }

        // 인식 못한(또는 비-트랜잭션) 이벤트는 안전 기본값으로
        return new PortoneEvent(null, null, "UNRECOGNIZED", null, null);
    }

    /** 트랜잭션 웹훅 → 이벤트 문자열 매핑 */
    private static String inferWebhookEventType(WebhookTransaction tx) {
        if (tx instanceof io.portone.sdk.server.webhook.WebhookTransactionReady)              return "READY";
        if (tx instanceof io.portone.sdk.server.webhook.WebhookTransactionPayPending)         return "PAY_PENDING";
        if (tx instanceof io.portone.sdk.server.webhook.WebhookTransactionPaid)               return "PAID";
        if (tx instanceof io.portone.sdk.server.webhook.WebhookTransactionCancelledPartialCancelled) return "PARTIAL_CANCELLED";
        if (tx instanceof io.portone.sdk.server.webhook.WebhookTransactionCancelledCancelled) return "CANCELLED";
        if (tx instanceof io.portone.sdk.server.webhook.WebhookTransactionFailed)             return "FAILED";
        return "UNRECOGNIZED";
    }


    @SuppressWarnings("unchecked")
    private static <T> T invokeOrNull(Object target, String method, Class<T> type) {
        if (target == null) return null;
        try {
            var m = target.getClass().getMethod(method);
            Object v = m.invoke(target);
            if (v == null) return null;
            if (type.isInstance(v)) return (T) v;
            // Kotlin primitive(Long) ↔ boxed 타입 보정
            if (type == Long.class && v instanceof Number n) return (T) Long.valueOf(n.longValue());
            if (type == Integer.class && v instanceof Number n2) return (T) Integer.valueOf(n2.intValue());
            if (type == String.class) return (T) String.valueOf(v);
            return null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }


    /** 취소/부분취소 계열에서 변경 금액 추출 (없으면 null) */
    private static Long extractWebhookChangedAmount(WebhookTransaction tx) {
        Object d = tx.getData();
        if (d == null) return null;

        // 경로 1) data.cancellation.totalAmount
        Object cancellation = invokeOrNull(d, "getCancellation", Object.class);
        if (cancellation != null) {
            Long totalAmount = invokeOrNull(cancellation, "getTotalAmount", Long.class);
            if (totalAmount != null) return totalAmount;

            // 경로 1-보정) data.cancellation.amount.total
            Object amountObj = invokeOrNull(cancellation, "getAmount", Object.class);
            if (amountObj != null) {
                Long total = invokeOrNull(amountObj, "getTotal", Long.class);
                if (total != null) return total;
            }
        }

        // 경로 2) data.totalAmount
        Long totalAmount = invokeOrNull(d, "getTotalAmount", Long.class);
        if (totalAmount != null) return totalAmount;

        // 경로 3) data.amount.total
        Object amountObj = invokeOrNull(d, "getAmount", Object.class);
        if (amountObj != null) {
            Long total = invokeOrNull(amountObj, "getTotal", Long.class);
            if (total != null) return total;
        }

        // 다른 버전/타입의 여지를 위해 null
        return null;    }

    /** 취소 계열에서 cancellationId 추출 (없으면 null) */
    private static String extractWebhookCancellationId(io.portone.sdk.server.webhook.WebhookTransaction tx) {
        Object d = tx.getData();
        if (d == null) return null;

        // 경로 1) data.cancellation.id
        Object cancellation = invokeOrNull(d, "getCancellation", Object.class);
        if (cancellation != null) {
            String id = invokeOrNull(cancellation, "getId", String.class);
            if (id != null) return id;
        }

        // 경로 2) data.cancellationId
        String id = invokeOrNull(d, "getCancellationId", String.class);
        if (id != null) return id;

        return null;
    }

    /** 웹훅 공통 DTO – 비즈니스 레이어가 SDK 타입 의존 없이 처리 가능 */
    public record PortoneEvent(
            String paymentId,        // 우리 Payment.providerPaymentId 로 매핑
            String transactionId,    // PaymentHistory.providerTransactionId 로 매핑
            String eventType,        // READY / PAY_PENDING / PAID / PARTIAL_CANCELLED / CANCELLED / FAILED
            Long   changedAmount,    // 취소/정산 등 금액 변화 있을 때만 세팅
            String cancellationId    // 취소 계열에서만 세팅
    ) {}



    /** 공통 에러 DTO */
    public record PaymentError(String code, String message, int httpStatus) {}
}
