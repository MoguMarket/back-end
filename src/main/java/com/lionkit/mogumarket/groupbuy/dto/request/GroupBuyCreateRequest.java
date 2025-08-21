package com.lionkit.mogumarket.groupbuy.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GroupBuyCreateRequest {

    @NotNull
    private Long productId;

    @Min(1)
    private double targetQty;

    /** 0 ~ 90 권장 */
    @Min(0)
    private double maxDiscountPercent;

    /** 옵션: 단계 수 (기본 3) */
    private Integer stageCount;
}