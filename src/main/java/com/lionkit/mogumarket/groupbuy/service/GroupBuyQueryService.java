package com.lionkit.mogumarket.groupbuy.service;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStatus;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyStageRepository;
import com.lionkit.mogumarket.product.dto.response.ProductGroupBuyOverviewResponse;
import com.lionkit.mogumarket.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupBuyQueryService {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyStageRepository stageRepository;

    /** 마감 임박순: OPEN && endAt > now, endAt ASC */
    public Page<ProductGroupBuyOverviewResponse> listClosingSoon(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("endAt").ascending());
        LocalDateTime now = LocalDateTime.now();
        Page<GroupBuy> gbPage = groupBuyRepository.findByStatusAndEndAtAfter(GroupBuyStatus.OPEN, now, pageable);

        return gbPage.map(this::toOverview);
    }

    private ProductGroupBuyOverviewResponse toOverview(GroupBuy gb) {
        Product p = gb.getProduct();
        var stages = stageRepository.findByGroupBuyOrderByStartQtyAsc(gb);
        int stageCount = stages.size();

        double totalQty = gb.getCurrentQty();

        Double currentDiscount = stages.stream()
                .filter(s -> totalQty >= s.getStartQty())
                .max(Comparator.comparingDouble(GroupBuyStage::getStartQty))
                .map(GroupBuyStage::getDiscountPercent)
                .orElse(0.0);

        Double remainingToNext = stages.stream()
                .filter(s -> totalQty < s.getStartQty())
                .min(Comparator.comparingDouble(GroupBuyStage::getStartQty))
                .map(s -> s.getStartQty() - totalQty)
                .orElse(null);

        long appliedUnitPrice = Math.round(
                p.getOriginalPricePerBaseUnit() * ((100.0 - currentDiscount) / 100.0)
        );

        return ProductGroupBuyOverviewResponse.builder()
                .productId(p.getId())
                .name(p.getName())
                .unit(p.getUnit().name())
                .originalPricePerBaseUnit(p.getOriginalPricePerBaseUnit())
                .stock(p.getStock())
                .imageUrl(p.getImageUrl())
                .storeId(p.getStore() == null ? null : p.getStore().getId())
                .storeName(p.getStore() == null ? null : p.getStore().getName())
                .unitType(p.getUnit()) // 필요시 유지

                .groupBuyId(gb.getId())
                .groupBuyStatus(gb.getStatus())
                .targetQty(gb.getTargetQty())
                .currentQty(gb.getCurrentQty())
                .maxDiscountPercent(gb.getMaxDiscountPercent())
                .stageCount(stageCount)

                .currentDiscountPercent(currentDiscount)
                .appliedUnitPrice(appliedUnitPrice)
                .remainingToNextStage(remainingToNext)
                .build();
    }
}