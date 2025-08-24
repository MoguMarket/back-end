package com.lionkit.mogumarket.cart.dto.request;

import com.lionkit.mogumarket.cart.enums.PurchaseRoute;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CartBulkSetRequest(
        @NotNull List<Line> lines
) {
    public record Line(
            @NotNull Long productId,
            @Min(0) double qtyBase, // 0이면 해당 라인 삭제 처리(정책)
            /** NORMAL(즉시구매) or GROUP_BUY(공구참여) */
            @NotNull
             PurchaseRoute route
    ) {}
}

