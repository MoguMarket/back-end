package com.lionkit.mogumarket.payment.service;

import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.order.entity.OrderLine;
import com.lionkit.mogumarket.order.entity.Orders;
import com.lionkit.mogumarket.order.enums.OrderStatus;
import com.lionkit.mogumarket.order.repository.OrderRepository;
import com.lionkit.mogumarket.order.service.OrderService;
import com.lionkit.mogumarket.payment.adapter.PaymentGateway;
import com.lionkit.mogumarket.payment.dto.response.PaymentSnapshotResponse;
import com.lionkit.mogumarket.payment.entity.Payment;
import com.lionkit.mogumarket.payment.entity.PaymentHistory;
import com.lionkit.mogumarket.payment.entity.PaymentLine;
import com.lionkit.mogumarket.payment.enums.*;
import com.lionkit.mogumarket.payment.dto.request.RefundSingleLineRequest;
import com.lionkit.mogumarket.payment.dto.response.LineRefundResponse;
import com.lionkit.mogumarket.payment.portone.enums.PortoneTransactionEventType;
import com.lionkit.mogumarket.payment.portone.entity.PortonePaymentDetail;
import com.lionkit.mogumarket.payment.portone.entity.PortonePaymentHistoryDetail;
import com.lionkit.mogumarket.payment.repository.PaymentHistoryRepository;
import com.lionkit.mogumarket.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {


    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * Port / Adapter (PortOne 구현체 주입)
     */
    private final PaymentGateway gateway;



    /**
     * 주문에 대한 결제 준비(READY) 엔티티를 생성하고 merchantUid를 발급
     * - 클라이언트는 반환된 merchantUid로 포트원 결제창을 호출
     */
    public CreatePaymentResult createPaymentReady(
            Long ordersId,
            long paidCashAmount,
            long paidPointAmount,
            CurrencyCode currency
    ) {
        Orders orders = orderRepository.findById(ordersId)
                .orElseThrow(() -> new BusinessException(ExceptionType.ORDER_NOT_FOUND));

        long total = Math.addExact(paidCashAmount, paidPointAmount);
        if (total <= 0) throw new BusinessException(ExceptionType.INVALID_AMOUNT);

        // merchantUid = v2에서 PortOne paymentId로 사용됨(서버/웹훅 모두 동일 키)
        String merchantUid = genMerchantUid(ordersId);

        Payment payment = Payment.builder()
                .paymentProvider(PaymentProvider.PORTONE)
                .providerPaymentId(null)            // 아직 모름(결제 완료 후 서버 재조회로 채움)
                .currentPaymentStatus(PaymentStatus.READY)
                .paidCashAmount(paidCashAmount)
                .paidPointAmount(paidPointAmount)
                .amount(total)
                .refundType(null)
                .totalRefundAmount(0L)
                .currency(Objects.requireNonNull(currency))
                .orders(orders)
                .build();

        payment.setMerchantUid(merchantUid);

        // 결제-주문 라인 맵핑(환불 트래킹용)
        attachPaymentLines(payment, orders.getLines());

        paymentRepository.save(payment);

        // 주문 상태는 OrderService에서 이미 CONFIRMED로 만들었다고 가정(필요시 보정)
        if (orders.getStatus() == null) {
            orders.updateStatus(OrderStatus.CONFIRMED);
        }

        return new CreatePaymentResult(payment.getId(), merchantUid, total, currency);
    }

    private static String genMerchantUid(Long ordersId) {
        return "orders-" + ordersId + "-" + UUID.randomUUID();
    }

    private static void attachPaymentLines(Payment payment, List<OrderLine> lines) {
        for (OrderLine ol : lines) {
            PaymentLine pl = PaymentLine.builder()
                    .payment(payment)
                    .orderLine(ol)
                    .refundedAmount(0L)
                    .build();
            payment.getLines().add(pl);
        }
    }

    public record CreatePaymentResult(
            Long paymentId,
            String merchantUid,
            long amount,
            CurrencyCode currency
    ) {
    }


    /**
     * 포트원 웹훅 처리 흐름:
     * - 서명 검증 → 공통 이벤트 파싱
     * - 우리 Payment(merchantUid) 조회
     * - PortOne 서버 재조회(lookup)로 신뢰 가능한 스냅샷 확보
     * - Payment/Orders 상태 동기화 + 이력 적재
     */
    public void handlePortoneWebhook(
            String rawBody,
            @Nullable String msgIdHeader,
            @Nullable String signatureHeader,
            @Nullable String timestampHeader
    ) {
        // 1) 서명 검증 + 파싱(예외 발생 시 401/400 처리 컨트롤러에서)
        PaymentGateway.WebhookEvent ev = gateway.verifyAndParseWebhook(rawBody, msgIdHeader, signatureHeader, timestampHeader);

        // 2) 우리 Payment 찾기(merchantUid == PortOne paymentId)
        String merchantUid = ev.paymentId();
        if (merchantUid == null) {
            // 안전망: 이벤트에 paymentId 없는 경우는 무시(로그만)
            return;
        }

        Payment payment = paymentRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new BusinessException(ExceptionType.PAYMENT_NOT_FOUND));

        // 3) 서버 재조회(anti-tampering)
        PaymentGateway.PaymentLookupResult snap = gateway.lookupByMerchantUid(merchantUid);

        // 4) 동기화
        syncPaymentAndOrderFromSnapshot(payment, snap);

        // 5) 트랜잭션 이력 적재
        persistHistoryFromWebhook(payment, ev);
    }

    private void syncPaymentAndOrderFromSnapshot(Payment payment, PaymentGateway.PaymentLookupResult snap) {
        // providerPaymentId 보정(최초 채움)
        if (payment.getProviderPaymentId() == null && snap.paymentId() != null) {
            payment.setProviderPaymentId(snap.paymentId());
        }

        // 서버 스냅샷 기준 상태 반영
        PaymentStatus newStatus = snap.status();
        payment.setCurrentPaymentStatus(newStatus);

        // (선택) 금액/통화 보정 필요 시 여기에서 확인/보정 가능
        // - 예: 포트원 금액(snap.totalAmount()) != 우리 amount() → 감사로그 후 reconcile 정책

        Orders orders = payment.getOrders();
        switch (newStatus) {
            case PAID -> {
                orders.updateStatus(OrderStatus.PAID);
                // PortOne 결제 상세(필요 시 저장)
                ensurePortoneDetail(payment, snap); // ← 스냅샷으로 실제값 주입
            }
            case PARTIAL_CANCELLED -> {
                // 주문은 유지(PAID)하되 정산만 변경되는 케이스
                if (orders.getStatus() == OrderStatus.PAID) {
                    // keep
                } else {
                    orders.updateStatus(OrderStatus.PAID);
                }
            }
            case CANCELLED, FAILED -> {
                // 주문 실패/취소 처리
                orderService.rollbackStocks(orders.getId());
            }
            case READY, PAY_PENDING -> {
                // 대기 상태 → 주문은 CONFIRMED 유지
                if (orders.getStatus() == null) {
                    orders.updateStatus(OrderStatus.CONFIRMED);
                }
            }
        }
    }

    private void ensurePortoneDetail(Payment payment, PaymentGateway.PaymentLookupResult snap) {
        if (payment.getPortonePaymentDetail() != null) return;

        PortonePaymentDetail d = PortonePaymentDetail.builder()
                .payment(payment)
                .portonePaymentMethod(snap.paymentMethod())      // 예: CARD / TRANSFER …
                .storeId(snap.storeId())                  // 포트원 storeId
                .pgVendor(snap.pgVendor())                // 예: KG_INICIS …
                .paymentRequestedAt(snap.requestedAt())   // Instant
                .build();
        payment.attachPortoneDetail(d);
    }

    private void persistHistoryFromWebhook(Payment payment, PaymentGateway.WebhookEvent ev) {
        // PaymentHistory(트랜잭션) upsert
        String txId = ev.transactionId();
        if (txId == null) return;

        PaymentHistory ph = paymentHistoryRepository.findByProviderTransactionIdAndProvider(txId, payment.getPaymentProvider())
                .orElseGet(() -> {
                    PaymentHistory created = PaymentHistory.builder()
                            .providerTransactionId(txId)
                            .payment(payment)
                            .provider(PaymentProvider.PORTONE)
                            .build();
                    return paymentHistoryRepository.save(created);
                });

        // 상세 이력(PortOne) 추가
        PortonePaymentHistoryDetail detail = PortonePaymentHistoryDetail.builder()
                .paymentHistory(ph)
                .transactionEventType(mapToPortoneEvent(ev.status()))
                .transactionStatusChangedAt(Instant.now()) // 웹훅의 정확 타임스탬프가 필요하면 Facade 확장
                .reason(null)
                .build();

        ph.addPortonePaymentHistoryDetail(detail);
        paymentHistoryRepository.save(ph);  // << 재저장으로 child persist 보장

    }

    private static PortoneTransactionEventType mapToPortoneEvent(PaymentStatus s) {
        return switch (s) {
            case READY -> PortoneTransactionEventType.TRANSACTION_READY;
            case PAY_PENDING -> PortoneTransactionEventType.TRANSACTION_PAY_PENDING;
            case PAID -> PortoneTransactionEventType.TRANSACTION_PAID;
            case PARTIAL_CANCELLED -> PortoneTransactionEventType.TRANSACTION_PARTIAL_CANCELLED;
            case CANCELLED -> PortoneTransactionEventType.TRANSACTION_CANCELLED;
            case FAILED -> PortoneTransactionEventType.TRANSACTION_FAILED;
        };
    }


    /**
     * paymentline 단위로 환불
     */
    @Transactional
    public LineRefundResponse cancelByLines(
            Long paymentId,
            List<RefundSingleLineRequest> requests,
            String reason,
            RefundType refundType
    ) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(ExceptionType.INVALID_AMOUNT); // 적절한 코드 사용
        }

        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ExceptionType.PAYMENT_NOT_FOUND));

        // 결제 단위 환불 방법 일관성 보장(현금/포인트)
        p.ensureRefundType(Objects.requireNonNull(refundType));

        // 1) 요청 검증 + 총액 계산
        Map<Long, PaymentLine> byOrderLineId = p.getLines().stream()
                .collect(java.util.stream.Collectors.toMap(pl -> pl.getOrderLine().getId(), pl -> pl));

        long requestedTotal = 0L;
        for (RefundSingleLineRequest req : requests) {
            if (req.refundAmount() <= 0) throw new BusinessException(ExceptionType.INVALID_AMOUNT);

            PaymentLine pl = byOrderLineId.get(req.orderLineId());
            if (pl == null) throw new BusinessException(ExceptionType.ORDER_LINE_NOT_FOUND);

            // 라인 최대 환불 가능액 = 라인 총액 - 이미 환불된 금액
            long lineGross = Math.round(pl.getOrderLine().getUnitPriceSnapshot() * pl.getOrderLine().getOrderedBaseQty());
            long remainingLine = lineGross - pl.getRefundedAmount();
            if (req.refundAmount() > remainingLine) {
                throw new BusinessException(ExceptionType.REFUND_EXCEEDS_AVAILABLE);
            }

            requestedTotal = Math.addExact(requestedTotal, req.refundAmount());
        }

        // 결제 단위 한도(현금/포인트)에 대한 가드
        long refundableAtPayment = (refundType == RefundType.CASH)
                ? p.getPaidCashAmount() : p.getPaidPointAmount();
        long remainingAtPayment = refundableAtPayment - p.getTotalRefundAmount();
        if (requestedTotal > remainingAtPayment) {
            throw new BusinessException(ExceptionType.REFUND_EXCEEDS_AVAILABLE);
        }

        // 2) PG 부분취소 호출 (PortOne)
        String providerPid = requireProviderPaymentId(p);
        var res = gateway.cancel(providerPid, requestedTotal, reason);

        // 3) DB 반영
        // 3-1) 결제 합계 환불액
        p.addToTotalRefund(res.cancelledAmount());

        // 3-2) 라인별 환불액 갱신
        for (RefundSingleLineRequest req : requests) {
            PaymentLine pl = byOrderLineId.get(req.orderLineId());
            pl.updateRefundedAmount(pl.getRefundedAmount() + req.refundAmount());
        }

        // 3-3) 상태 전이
        long refunded = p.getTotalRefundAmount();
        long limit = refundableAtPayment;
        if (refunded < limit) {
            p.setCurrentPaymentStatus(PaymentStatus.PARTIAL_CANCELLED);
        } else {
            p.setCurrentPaymentStatus(PaymentStatus.CANCELLED);
            p.getOrders().updateStatus(OrderStatus.FAILED);
            // 필요시 재고/공구 롤백 (결제 전액 현금 환불 완료 시점에만)
            orderService.rollbackStocks(p.getOrders().getId());
        }



        return LineRefundResponse.builder()
                .cancellationId(res.cancellationId())
                .cancelledAmount(res.cancelledAmount())
                .status(p.getCurrentPaymentStatus()).build();
    }


    /**
     * upsertHistoryForCancel 는 별도로 구현하지 않습니다.
     * 히스토리는 웹훅이 오면 persistHistoryFromWebhook(...)에 의헤
     * transactionId 기반으로 한 번만 기록합니다. ( 중복 저장 방지 )
     */
    private static String requireProviderPaymentId(Payment p) {
        String id = p.getProviderPaymentId();
        if (id == null) {
            // v2에선 merchantUid == providerPaymentId 이므로 보정
            id = p.getMerchantUid();
        }
        if (id == null) throw new PaymentGateway.PaymentGatewayException("No providerPaymentId / merchantUid bound");
        return id;
    }




    /**
     * 요약 조회
     * @param paymentId
     * @return
     */
    @Transactional(readOnly = true)
    public PaymentSnapshotResponse getSnapshot(Long paymentId) {
        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ExceptionType.PAYMENT_NOT_FOUND));

        return new PaymentSnapshotResponse(
                p.getId(),
                p.getOrders().getId(),
                p.getMerchantUid(),
                p.getPaymentProvider().getKey(),
                p.getCurrentPaymentStatus().getKey(),
                p.getAmount(),
                p.getPaidCashAmount(),
                p.getPaidPointAmount(),
                p.getTotalRefundAmount(),
                p.getCurrency()
        );
    }




}
