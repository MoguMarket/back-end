package com.lionkit.mogumarket.recommend.dto.request;

import com.lionkit.mogumarket.product.enums.Unit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ListingReviewRequest {

    @Schema(description = "기존 상품 ID(선택). 주면 DB값으로 일부 보정", example = "101")
    private Long productId;

    @NotBlank
    @Schema(description = "상품명", example = "제주 당근 5kg")
    private String name;

    @NotBlank
    @Schema(description = "설명", example = "달달한 봄 당근")
    private String description;

    @NotNull
    @Schema(description = "단위", example = "KG")
    private Unit unit;

    @Positive
    @Schema(description = "정가/원가(기준단위당)", example = "3200")
    private double originalPricePerBaseUnit;

    @PositiveOrZero
    @Schema(description = "재고(기준단위)", example = "500")
    private double stock;

    @PositiveOrZero
    @Schema(description = "최대 할인율(%)", example = "15")
    private Double maxDiscountPercent;

    @Positive
    @Schema(description = "공구 단계 수", example = "5")
    private Integer stageCount;

    @Schema(description = "통화(기본 KRW)", example = "KRW")
    private String currency;
}