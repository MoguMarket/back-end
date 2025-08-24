package com.lionkit.mogumarket.product.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * product 엔티티 관련 전반적인 내용을 resposne
 */
@Builder
public record ProductOverviewResponse(

        // 기본 상품 정보
        BasicInfo basicInfo,

        // 매핑된 가게 정보
        StoreInfo storeInfo,

        // 공동구매 정보
        GroupBuyInfo groupBuyInfo,

        // 가격 정보
        PriceInfo priceInfo,

        // 할인율 정보
        DiscountInfo discountInfo,

        // 재고 정보
        StockInfo stockInfo,

        // 주문 수량
        OrderInfo orderInfo,

        // 공구 달성률
        ProgressInfo progressInfo
) {

    /**
     * 기본 상품 정보
     */
    @Builder
    public record BasicInfo(
            Long productId,   // 상품 ID
            String name,      // 상품명
            String unit,      // 단위
            String imageUrl   // 상품 이미지 URL
    ) {}

    /**
     * 매핑된 가게 정보
     */
    @Builder
    public record StoreInfo(
            Long storeId,     // 매핑된 store ID
            String storeName  // 매핑된 store 이름
    ) {}

    /**
     * 공동구매 정보
     */
    @Builder
    public record GroupBuyInfo(
            Long groupBuyId,          // 공동구매 ID
            List<StageBrief> stages   // 공동구매 스테이지 요약
    ) {}

    /**
     * 가격 정보
     */
    @Builder
    public record PriceInfo(
            double originalPricePerBaseUnit,     // 기준단위당 일반 구매용 가격
            double basePricePerBaseUnitSnapshot, // 기준단위당 공동 구매용 ‘원가’
            double appliedUnitPrice              // 기준단위당 공동 구매용 현재 할인 적용된 가격
    ) {}

    /**
     * 할인율 정보
     */
    @Builder
    public record DiscountInfo(
            double maxDiscountPercent, // 공동구매 최대 할인율 (GroupBuy.maxDiscountPercent)
            double discountPercent     // 현재 적용된 할인율 (GroupBuyStage.discountPercent)
    ) {}

    /**
     * 재고 정보
     */
    @Builder
    public record StockInfo(
            double stock,         // 전체 재고 (일반 구매 + 공구)
            double aloneBuyStock, // 일반 구매 가능 재고 (stock - product.currentBaseQty)
            double groupBuyStock  // 공동 구매 가능 재고 (targetQty - currentQty)
    ) {}

    /**
     * 주문 수량
     */
    @Builder
    public record OrderInfo(
            double currentBaseQty,    // 전체 주문수량 (일반 주문 + 공구 주문)
            double currentProductQty, // 일반 누적 구매 수량 (currentBaseQty - currentQty)
            double currentQty         // 공구 누적 구매 수량
    ) {}

    /**
     * 공구 달성률
     */
    @Builder
    public record ProgressInfo(
            double remainingToNextStage, // 다음 스테이지까지 남은 공구 수량
            double progressPercent       // 공구 달성률 (0~100)
    ) {}

    /**
     * 스테이지 요약 정보
     */
    @Builder
    public record StageBrief(
            int level,               // 스테이지 레벨
            double startQty,         // 시작 수량
            double discountPercent,  // 해당 스테이지 할인율
            long appliedUnitPrice    // 해당 스테이지 적용 단가
    ) {}
}
