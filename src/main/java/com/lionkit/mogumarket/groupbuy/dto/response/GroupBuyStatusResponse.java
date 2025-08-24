package com.lionkit.mogumarket.groupbuy.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.lionkit.mogumarket.groupbuy.enums.GroupBuyStatus;
import lombok.Builder;

@Builder
public record GroupBuyStatusResponse(
        Long groupBuyId,
        GroupBuyStatus status,

        // 누적/목표
        double totalQuantity,
        double targetQuantity,

        // 현재/다음 단계 정보
        Integer currentStageLevel,      // 없으면 null
        Double currentStageStartQty,    // 없으면 null
        Double currentDiscount,         // %
        Long appliedUnitPrice,          // 현재 적용 단가(스냅샷 기준)

        Integer nextStageLevel,         // 없으면 null
        Double nextStageStartQty,       // 없으면 null
        Double remainingToNextStage,    // 다음 단게까지 남은 수량, 없으면 0

        // 원가/최대 할인 등 참고 값
        double originalUnitPrice,       // basePricePerBaseUnitSnapshot
        double maxDiscountPercent,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        java.time.LocalDateTime lastUpdatedAt // 필요 시 버전/업데이트 표기
) {}
