package com.lionkit.mogumarket.groupbuy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateGroupBuyRequest(

        @Schema(description = "상품 ID", example = "1")
        @NotNull Long productId,

        @Schema(description = "목표 수량", example = "10")
        @NotNull @Positive Double targetQty,

        @Schema(description = "최대 할인률(%)", example = "20")
        @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "100.0", inclusive = false)
        Double maxDiscountPercent,

        @Schema(description = "단계 수(기본 3)", example = "3")
        @NotNull @Min(1) @Max(10) Integer stage
) {}
