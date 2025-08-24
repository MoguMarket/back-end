package com.lionkit.mogumarket.payment.dto.response;

import com.lionkit.mogumarket.payment.enums.CurrencyCode;
import com.lionkit.mogumarket.payment.dto.PaymentSnapshot;
import lombok.Builder;

@Builder
public record PaymentSnapshotResponse(
        Long paymentId,
        Long ordersId,
        String merchantUid,
        String provider,
        String status,
        long amount,
        long paidCashAmount,
        long paidPointAmount,
        long totalRefundAmount,
        CurrencyCode currency
) {
    public static PaymentSnapshotResponse from(PaymentSnapshot s) {
        return PaymentSnapshotResponse.builder()
                .paymentId(s.paymentId())
                .ordersId(s.ordersId())
                .merchantUid(s.merchantUid())
                .provider(s.provider() != null ? s.provider().name() : null)
                .status(s.status() != null ? s.status().name() : null)
                .amount(s.amount())
                .paidCashAmount(s.paidCashAmount())
                .paidPointAmount(s.paidPointAmount())
                .totalRefundAmount(s.totalRefundAmount())
                .currency(s.currency())
                .build();
    }
}