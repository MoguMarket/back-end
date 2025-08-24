package com.lionkit.mogumarket.payment.dto.response;

import com.lionkit.mogumarket.payment.enums.PaymentStatus;
import lombok.Builder;

@Builder
public record LineRefundResponse(
        String cancellationId,
        long cancelledAmount,
        PaymentStatus status
) {
}