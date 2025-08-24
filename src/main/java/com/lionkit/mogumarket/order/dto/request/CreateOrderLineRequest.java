package com.lionkit.mogumarket.order.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** 주문 라인 단위 요청입니다. */
public record CreateOrderLineRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @NotNull(message = "수량은 필수입니다.")
        @DecimalMin(value = "1.0", message = "수량은 1 이상이어야 합니다.")
        Double qtyBase,

        @NotNull(message = "공구 참여/개인 구매 여부는 필수입니다.")
        Boolean participateInGroupBuy
) {}