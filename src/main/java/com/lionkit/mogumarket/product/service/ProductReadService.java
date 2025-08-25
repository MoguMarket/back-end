package com.lionkit.mogumarket.product.service;

import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyStageRepository;
import com.lionkit.mogumarket.product.dto.response.ProductOverviewResponse;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.enums.SortType;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReadService {

    private final ProductRepository productRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyStageRepository stageRepository;




    @Transactional(readOnly = true)
    public ProductOverviewResponse getOverview(Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));

        GroupBuy gb = p.getGroupBuy();
        if (gb == null) throw new BusinessException(ExceptionType.GROUP_BUY_NOT_OPEN);

        // 스테이지 목록(오름차순)
        List<GroupBuyStage> stages = stageRepository.findByGroupBuyOrderByStartQtyAsc(gb);
        double curQty = gb.getCurrentQty();

        // 현재 스테이지: 없으면 베이스 스테이지(할인 0, 단가 = base snapshot)로 간주
        var curOpt = stageRepository
                .findTopByGroupBuyAndStartQtyLessThanEqualOrderByStartQtyDesc(gb, curQty);

        double discountPercent = curOpt.map(GroupBuyStage::getDiscountPercent).orElse(0d);
        double appliedUnitPrice = curOpt.map(GroupBuyStage::getAppliedUnitPrice)
                .orElse(gb.getBasePricePerBaseUnitSnapshot());

        // 다음 스테이지
        GroupBuyStage nextStage = stages.stream()
                .filter(s -> curQty < s.getStartQty())
                .findFirst()
                .orElse(null);
        double remainingToNext = (nextStage == null) ? 0d : Math.max(0d, nextStage.getStartQty() - curQty);

        // 브리프 변환
        var stageBriefs = stages.stream()
                .map(s -> new ProductOverviewResponse.StageBrief(
                        s.getLevel(),
                        s.getStartQty(),
                        s.getDiscountPercent(),
                        s.getAppliedUnitPrice()
                ))
                .toList();

        // 재고 및 진행률
        double aloneBuyRemain = Math.max(0d, p.getStock() - p.getCurrentBaseQty());
        double groupBuyRemain = Math.max(0d, gb.getTargetQty() - gb.getCurrentQty());
        double totalOrdered = p.getCurrentBaseQty();
        double groupOrdered = gb.getCurrentQty();
        double normalOrdered = Math.max(0d, totalOrdered - groupOrdered);
        double progressPercent = (gb.getTargetQty() > 0d)
                ? Math.min(100d, (groupOrdered / gb.getTargetQty()) * 100d)
                : 0d;

        return ProductOverviewResponse.builder()
                .basicInfo(ProductOverviewResponse.BasicInfo.builder()
                        .productId(p.getId())
                        .name(p.getName())
                        .unit(p.getUnit().name())
                        .imageUrl(p.getImageUrl())
                        .build())
                .storeInfo(ProductOverviewResponse.StoreInfo.builder()
                        .storeId(p.getStore().getId())
                        .storeName(p.getStore().getName())
                        .build())
                .groupBuyInfo(ProductOverviewResponse.GroupBuyInfo.builder()
                        .groupBuyId(gb.getId())
                        .stages(stageBriefs)
                        .build())
                .priceInfo(ProductOverviewResponse.PriceInfo.builder()
                        .originalPricePerBaseUnit(p.getOriginalPricePerBaseUnit())
                        .basePricePerBaseUnitSnapshot(gb.getBasePricePerBaseUnitSnapshot())
                        .appliedUnitPrice(appliedUnitPrice)
                        .build())
                .discountInfo(ProductOverviewResponse.DiscountInfo.builder()
                        .maxDiscountPercent(gb.getMaxDiscountPercent())
                        .discountPercent(discountPercent)
                        .build())
                .stockInfo(ProductOverviewResponse.StockInfo.builder()
                        .stock(p.getStock())
                        .aloneBuyStock(aloneBuyRemain)
                        .groupBuyStock(groupBuyRemain)
                        .build())
                .orderInfo(ProductOverviewResponse.OrderInfo.builder()
                        .currentBaseQty(totalOrdered)
                        .currentProductQty(normalOrdered)
                        .currentQty(groupOrdered)
                        .build())
                .progressInfo(ProductOverviewResponse.ProgressInfo.builder()
                        .remainingToNextStage(remainingToNext)
                        .progressPercent(progressPercent)
                        .build())
                .build();
    }


    /**
     * 상품 필터링 서비스
     * SortType에 따라 상품을 정렬하여 반환합니다.
     */
    public List<ProductResponse> filterProducts(SortType sortType) {
        Sort sort = Sort.by(sortType.getDirection(), sortType.getField());
        List<Product> products = productRepository.findAll(sort);

        return products.stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }


    @Transactional(readOnly = true)
    public Page<ProductResponse> listByCategory(CategoryType category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository
                .findByCategoryOrderByCreatedAtDesc(category, pageable)
                .map(ProductResponse::fromEntity);
    }

    // (선택) 다중 카테고리
    @Transactional(readOnly = true)
    public Page<ProductResponse> listByCategories(List<CategoryType> categories, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository
                .findByCategoryInOrderByCreatedAtDesc(categories, pageable)
                .map(ProductResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listByPrice(Double min, Double max, int page, int size) {
        double lo = (min == null) ? 0 : min;
        double hi = (max == null) ? Double.MAX_VALUE : max;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository
                .findByOriginalPricePerBaseUnitBetweenOrderByCreatedAtDesc(lo, hi, pageable)
                .map(ProductResponse::fromEntity);
    }
}