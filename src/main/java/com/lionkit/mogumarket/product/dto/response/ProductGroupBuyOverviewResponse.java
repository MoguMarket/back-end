package com.lionkit.mogumarket.product.dto.response;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStatus;
import com.lionkit.mogumarket.product.enums.Unit;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProductGroupBuyOverviewResponse(
        Long productId,
        String name,
        String unit,
        double originalPricePerBaseUnit,
        double stock,
        String imageUrl,
        Long storeId, // 매핑된 store ID
        String storeName,// 매핑된 store
        Unit unitType, // 단위 (추가 필요시)


        // 공구가 없으면 null/0 으로 내려감
        Long groupBuyId,
        GroupBuyStatus groupBuyStatus,
        Double targetQty,
        Double currentQty,
        Double maxDiscountPercent,
        int stageCount,
        LocalDateTime startAt, // 시작 시간
        LocalDateTime endAt, // 마감 시간


        // 계산 정보
        Double currentDiscountPercent,
        Long appliedUnitPrice,
        Double remainingToNextStage
) {}