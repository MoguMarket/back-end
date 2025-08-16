
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
        double qty = product.getCurrentBaseQty();
        return stageRepository
                .findTopByProductAndStartBaseQtyLessThanEqualOrderByStartBaseQtyDesc(product, qty);
    }

    /** 다음 단계 조회 (없으면 null) */
    @Transactional(readOnly = true)
    public ProductStage getNextStage(Product product) {
        double qty = product.getCurrentBaseQty();
        return stageRepository
                .findTopByProductAndStartBaseQtyGreaterThanOrderByStartBaseQtyAsc(product, qty);
    }

    /** 다음 단계까지 남은 수량 (없으면 null) */
    @Transactional(readOnly = true)
    public Double remainingToNext(Product product) {
        ProductStage next = getNextStage(product);
        if (next == null) return null;
        return next.getStartBaseQty() - product.getCurrentBaseQty();
    }

    /** 현재 단계 기준 단가 계산 (정가 * (1 - 할인율%)) */
    @Transactional(readOnly = true)
    public long getAppliedUnitPrice(Product product) {
        ProductStage cur = getCurrentStage(product);
        double discountPercent = (cur == null) ? 0.0 : cur.getDiscountPercent(); // double 기반
        double rate = (100.0 - discountPercent) / 100.0;
        return Math.round(product.getOriginalPricePerBaseUnit() * rate);
    }
}
