package com.lionkit.mogumarket.product.service;

import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.entity.ProductStage;
import com.lionkit.mogumarket.product.repository.ProductStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductStageService {

    private final ProductStageRepository stageRepository;

    /** 현재 단계 조회 (없으면 null) */
    @Transactional(readOnly = true)
    public ProductStage getCurrentStage(Product product) {
        if (product == null) return null;
        double qty = product.getCurrentBaseQty();
        return stageRepository
                .findTopByProductAndStartBaseQtyLessThanEqualOrderByStartBaseQtyDesc(product, qty);
    }

    /** 다음 단계 조회 (없으면 null) */
    @Transactional(readOnly = true)
    public ProductStage getNextStage(Product product) {
        if (product == null) return null;
        double qty = product.getCurrentBaseQty();
        return stageRepository
                .findTopByProductAndStartBaseQtyGreaterThanOrderByStartBaseQtyAsc(product, qty);
    }

    /** 다음 단계까지 남은 수량 (없으면 null) */
    @Transactional(readOnly = true)
    public Double remainingToNext(Product product) {
        if (product == null) return null;
        ProductStage next = getNextStage(product);
        if (next == null) return null;
        return next.getStartBaseQty() - product.getCurrentBaseQty();
    }

    /**
     * 현재 단계 기준 단가(원화) — 반올림하여 long으로 반환.
     * Product.originalPricePerBaseUnit 이 double 이므로 단계 할인율 적용 후 Math.round 사용.
     */
    @Transactional(readOnly = true)
    public long getAppliedUnitPrice(Product product) {
        if (product == null) return 0L;
        double discounted = getAppliedUnitPriceRaw(product);
        return Math.round(discounted);
    }

    /** 현재 단계 기준 단가(실수값, 원 단위) — 필요 시 사용 */
    @Transactional(readOnly = true)
    public double getAppliedUnitPriceRaw(Product product) {
        if (product == null) return 0.0;
        ProductStage cur = getCurrentStage(product);
        double discountPercent = (cur == null) ? 0.0 : cur.getDiscountPercent(); // 0~100
        double rate = (100.0 - discountPercent) / 100.0;
        return product.getOriginalPricePerBaseUnit() * rate;
    }
}