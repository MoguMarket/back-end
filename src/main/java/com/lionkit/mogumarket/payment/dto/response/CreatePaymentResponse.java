package com.lionkit.mogumarket.payment.dto.response;

import com.lionkit.mogumarket.payment.enums.CurrencyCode;
import com.lionkit.mogumarket.payment.service.PaymentService;
import lombok.Builder;

@Builder
public record CreatePaymentResponse(
        Long paymentId,
        String merchantUid,
        long amount,
        CurrencyCode currency
) {
    public static CreatePaymentResponse from(PaymentService.CreatePaymentResult r) {
        return CreatePaymentResponse.builder()
                .paymentId(r.paymentId())
                .merchantUid(r.merchantUid())
                .amount(r.amount())
                .currency(r.currency())
                .build();
    }
}
