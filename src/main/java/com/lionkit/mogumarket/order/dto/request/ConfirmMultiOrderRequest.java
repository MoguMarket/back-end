package com.lionkit.mogumarket.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 여러 OrderLine을 한 번에 확정하는 요청 DTO입니다. */
public record ConfirmMultiOrderRequest(
        @NotNull @Size(min = 1, message = "최소 1개 이상의 주문 라인이 필요합니다.")
        List<@Valid CreateOrderLineRequest> lines
) {}