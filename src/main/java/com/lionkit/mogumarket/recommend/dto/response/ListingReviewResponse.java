package com.lionkit.mogumarket.recommend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ListingReviewResponse {

    @Schema(description = "권장 가격")
    private double recommendedPrice;

    @Schema(description = "권장 최저가")
    private double minRecommendedPrice;

    @Schema(description = "권장 최고가")
    private double maxRecommendedPrice;

    @Schema(description = "가격 산출 근거(최대 2줄)")
    private String reasoning;

    @Schema(description = "통화")
    private String currency;

    @Schema(description = "LLM 사용 여부(폴백이면 false)")
    private boolean fromLlm;

    @Schema(description = "필드별 수정 제안")
    private List<FieldSuggestion> suggestions;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FieldSuggestion {
        @Schema(description = "필드명 (name|description|unit|originalPricePerBaseUnit|stock|maxDiscountPercent|stageCount)")
        private String field;

        @Schema(description = "제안 값(사람이 그대로 넣기 쉬운 문구)")
        private String suggestedValue;

        @Schema(description = "짧은 근거(최대 2줄)")
        private String reason;
    }
}