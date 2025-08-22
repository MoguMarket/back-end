package com.lionkit.mogumarket.product.dto.response;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStatus;
import lombok.Builder;

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


        // 공구가 없으면 null/0 으로 내려감
        Long groupBuyId,
        GroupBuyStatus groupBuyStatus,
        Double targetQty,
        Double currentQty,
        Double maxDiscountPercent,

        // 계산 정보
        Double currentDiscountPercent,
        Long appliedUnitPrice,
        Double remainingToNextStage
) {}