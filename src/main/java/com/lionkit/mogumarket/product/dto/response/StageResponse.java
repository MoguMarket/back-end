package com.lionkit.mogumarket.product.dto.response;

import com.lionkit.mogumarket.product.entity.ProductStage;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StageResponse {
    private Long id;
    private int level;
    private double startBaseQty;
    private double discountPercent;

    public static StageResponse fromEntity(ProductStage stage) {
        if (stage == null) return null;
        return StageResponse.builder()
                .id(stage.getId())
                .level(stage.getLevel())
                .startBaseQty(stage.getStartBaseQty())
                .discountPercent(stage.getDiscountPercent())
                .build();
    }
}