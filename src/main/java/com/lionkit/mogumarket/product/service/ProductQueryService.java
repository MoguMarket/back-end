package com.lionkit.mogumarket.product.service;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStatus;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyStageRepository;
import com.lionkit.mogumarket.product.dto.response.ProductGroupBuyOverviewResponse;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyStageRepository stageRepository;

    public ProductGroupBuyOverviewResponse getOverview(Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + productId));

        // 최신 OPEN 공구 1건 스냅샷
        Optional<GroupBuy> maybeGb = groupBuyRepository
                .findTopByProductAndStatusOrderByCreatedAtDesc(p, GroupBuyStatus.OPEN);

        if (maybeGb.isEmpty()) {
            return ProductGroupBuyOverviewResponse.builder()
                    .productId(p.getId())
                    .name(p.getName())
                    .unit(p.getUnit().name())
                    .originalPricePerBaseUnit(p.getOriginalPricePerBaseUnit())
                    .stock(p.getStock())
                    .imageUrl(p.getImageUrl())
                    .groupBuyId(null)
                    .groupBuyStatus(null)
                    .targetQty(null)
                    .currentQty(null)
                    .maxDiscountPercent(null)
                    .currentDiscountPercent(0.0)
                    .appliedUnitPrice(Math.round(p.getOriginalPricePerBaseUnit()))
                    .remainingToNextStage(null)
                    .build();
        }

        GroupBuy gb = maybeGb.get();
        double totalQty = gb.getCurrentQty();

        // 현재 단계 할인
        Double currentDiscount = stageRepository.findByGroupBuyOrderByStartQtyAsc(gb).stream()
                .filter(s -> totalQty >= s.getStartQty())
                .max(Comparator.comparingDouble(GroupBuyStage::getStartQty))
                .map(GroupBuyStage::getDiscountPercent)
                .orElse(0.0);

        // 다음 단계까지 남은 수량
        Double remainingToNext = stageRepository.findByGroupBuyOrderByStartQtyAsc(gb).stream()
                .filter(s -> totalQty < s.getStartQty())
                .min(Comparator.comparingDouble(GroupBuyStage::getStartQty))
                .map(s -> s.getStartQty() - totalQty)
                .orElse(null);

        long appliedUnitPrice = Math.round(p.getOriginalPricePerBaseUnit() * ((100.0 - currentDiscount) / 100.0));

        return ProductGroupBuyOverviewResponse.builder()
                .productId(p.getId())
                .name(p.getName())
                .unit(p.getUnit().name())
                .originalPricePerBaseUnit(p.getOriginalPricePerBaseUnit())
                .stock(p.getStock())
                .imageUrl(p.getImageUrl())
                .groupBuyId(gb.getId())
                .groupBuyStatus(gb.getStatus())
                .targetQty(gb.getTargetQty())
                .currentQty(gb.getCurrentQty())
                .maxDiscountPercent(gb.getMaxDiscountPercent())
                .currentDiscountPercent(currentDiscount)
                .appliedUnitPrice(appliedUnitPrice)
                .remainingToNextStage(remainingToNext)
                .build();
    }
}