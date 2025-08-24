package com.lionkit.mogumarket.payment.dto.request;

import com.lionkit.mogumarket.payment.enums.RefundType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RefundLinesRequest(
        @NotEmpty List<@Valid RefundSingleLineRequest> lines,
        @NotNull RefundType refundType,
        @NotNull String reason
) {}
