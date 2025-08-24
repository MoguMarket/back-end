package com.lionkit.mogumarket.payment.dto.request;

import com.lionkit.mogumarket.payment.enums.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.lang.Nullable;

public record CreatePaymentRequest(
        @NotNull Long ordersId,
        @PositiveOrZero long paidCashAmount,
        @PositiveOrZero long paidPointAmount
) {}