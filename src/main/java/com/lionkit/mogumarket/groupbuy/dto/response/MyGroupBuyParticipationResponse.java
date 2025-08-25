package com.lionkit.mogumarket.groupbuy.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyUser;
import com.lionkit.mogumarket.groupbuy.enums.GroupBuyStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MyGroupBuyParticipationResponse(
        List<Entry> items,
        Integer totalCount // 선택: 페이징 안 쓰면 null 또는 items.size()
) {
    @Builder
    public record Entry(
            // GroupBuy 스냅샷
            Long groupBuyId,
            Long productId,
            String productName,                 // 선택
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

            // GroupBuyStage 리스트
            List<StageDto> stages,

            // 로그인 사용자의 참여 정보(GroupBuyUser)
            ParticipationDto participation
    ) {}

    @Builder
    public record StageDto(
            int level,
            double startQty,
            double discountPercent,
            double appliedUnitPrice
    ) {
        public static StageDto fromEntity(GroupBuyStage s) {
            return StageDto.builder()
                    .level(s.getLevel())
                    .startQty(s.getStartQty())
                    .discountPercent(s.getDiscountPercent())
                    .appliedUnitPrice(s.getAppliedUnitPrice())
                    .build();
        }
    }

    @Builder
    public record ParticipationDto(
            Long groupBuyUserId,
            double totalQty,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime lastJoinedAt
    ) {
        public static ParticipationDto fromEntity(GroupBuyUser gbu) {
            return ParticipationDto.builder()
                    .groupBuyUserId(gbu.getId())
                    .totalQty(gbu.getTotalQty())
                    .lastJoinedAt(gbu.getLastJoinedAt())
                    .build();
        }
    }

    public static Entry toEntry(GroupBuyUser gbu) {
        GroupBuy gb = gbu.getGroupBuy();
        return Entry.builder()
                .groupBuyId(gb.getId())
                .productId(gb.getProduct().getId())
                .productName(gb.getProduct().getName())
                .status(gb.getStatus())
                .startAt(gb.getStartAt())
                .endAt(gb.getEndAt())
                .targetQty(gb.getTargetQty())
                .currentQty(gb.getCurrentQty())
                .maxDiscountPercent(gb.getMaxDiscountPercent())
                .basePricePerBaseUnitSnapshot(gb.getBasePricePerBaseUnitSnapshot())
                .version(gb.getVersion())
                .stages(gb.getStages().stream().map(StageDto::fromEntity).toList())
                .participation(ParticipationDto.fromEntity(gbu))
                .build();
    }
}
