package com.lionkit.mogumarket.payment.dto;

import com.lionkit.mogumarket.payment.enums.CurrencyCode;
import com.lionkit.mogumarket.payment.enums.PaymentProvider;
import com.lionkit.mogumarket.payment.enums.PaymentStatus;

public record PaymentSnapshot(
        Long paymentId,
        Long ordersId,
        String merchantUid,
        PaymentProvider provider,
        PaymentStatus status,
        long amount,
        long paidCashAmount,
        long paidPointAmount,
        long totalRefundAmount,
        CurrencyCode currency
) {
}
