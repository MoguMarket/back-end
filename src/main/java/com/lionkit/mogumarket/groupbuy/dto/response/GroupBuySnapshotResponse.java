package com.lionkit.mogumarket.groupbuy.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.enums.GroupBuyStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GroupBuySnapshotResponse(
        Long id,
        Long productId,
        GroupBuyStatus status,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endAt,
        double targetQty,
        double currentQty,
        double maxDiscountPercent,
        double basePricePerBaseUnitSnapshot,
        Long version,
        List<GroupBuyStageSnapshotDto> stages
) {
    public static GroupBuySnapshotResponse fromEntity(GroupBuy gb) {
        return GroupBuySnapshotResponse.builder()
                .id(gb.getId())
                .productId(gb.getProduct().getId())
                .status(gb.getStatus())
                .startAt(gb.getStartAt())
                .endAt(gb.getEndAt())
                .targetQty(gb.getTargetQty())
                .currentQty(gb.getCurrentQty())
                .maxDiscountPercent(gb.getMaxDiscountPercent())
                .basePricePerBaseUnitSnapshot(gb.getBasePricePerBaseUnitSnapshot())
                .version(gb.getVersion())
                .stages(gb.getStages().stream().map(GroupBuyStageSnapshotDto::fromEntity).toList())
                .build();
    }

    @Builder
    public record GroupBuyStageSnapshotDto(
            int level,
            double startQty,
            double discountPercent,
            double appliedUnitPrice
    ) {
        public static GroupBuyStageSnapshotDto fromEntity(GroupBuyStage s) {
            return GroupBuyStageSnapshotDto.builder()
                    .level(s.getLevel())
                    .startQty(s.getStartQty())
                    .discountPercent(s.getDiscountPercent())
                    .appliedUnitPrice(s.getAppliedUnitPrice())
                    .build();
        }
    }
}
