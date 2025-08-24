package com.lionkit.mogumarket.product.entity;

import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.enums.GroupBuyStatus;
import com.lionkit.mogumarket.product.dto.ProductUpdateDto;
import com.lionkit.mogumarket.product.enums.Unit;
import com.lionkit.mogumarket.review.entity.Review;
import com.lionkit.mogumarket.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType category;

    /**
     * 기준단위당 '일반 구매용 ‘원가’.(!= 공구용 원가인 GroupBuy.basePricePerBaseUnitSnapshot ).
     * 공구 개설 이후 변경이 불가한 GroupBuy.basePricePerBaseUnitSnapshot 과는 달리
     * 수정이 가능합니다.
     */
    @Column(nullable = false)
    private double originalPricePerBaseUnit;

    /**
     * stock : 전체 재고 상한
     * 남은 재고 == stock - product.currentBaseQty
     */
    @Column(nullable = false)
    private double stock;

    private String imageUrl;


    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private GroupBuy groupBuy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /**
     * Product.currentBaseQty : 일반주문누적 + 공구누적
     */
    @Builder.Default
    @Column(name = "current_base_qty", nullable = false)
    private double currentBaseQty = 0;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();


    public boolean isGroupBuyOpen() {
        return groupBuy != null &&
                groupBuy.getStatus() == GroupBuyStatus.OPEN;
    }

    public double getRemainStock() {
        return stock - currentBaseQty;
    }


    public void update(ProductUpdateDto dto) {
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getDescription() != null) this.description = dto.getDescription();
        if (dto.getUnit() != null) this.unit = dto.getUnit();
        if (dto.getOriginalPricePerBaseUnit() != null) this.originalPricePerBaseUnit = dto.getOriginalPricePerBaseUnit(); //  일반 구매용 원가는 변경 가능
        if (dto.getStock() != null) this.stock = dto.getStock();
        if (dto.getImageUrl() != null) this.imageUrl = dto.getImageUrl();
        if (dto.getCategory() != null) this.category = dto.getCategory();

    }

    public void patch(Double originalPrice, String imageUrl) {
        if (originalPrice != null) this.originalPricePerBaseUnit = originalPrice;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }

    /**
     * 누적 수량 증가 (일반+공구 합산). 비관적 락 하에서 호출.
     */
    public void increaseCurrentBaseQty(double qtyBase) {
        if (qtyBase <= 0) throw new BusinessException(ExceptionType.INVALID_QTY);
        double next = this.currentBaseQty + qtyBase;
        if (next > this.stock) {
            throw new IllegalStateException("재고 초과: 요청=" + qtyBase + ", 남은=" + (this.stock - this.currentBaseQty));
        }
        this.currentBaseQty = next;
    }
}