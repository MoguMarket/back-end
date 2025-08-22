package com.lionkit.mogumarket.product.entity;

import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.global.base.domain.BaseEntity;
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
    @Column(nullable = true) // ← DB가 DEFAULT NULL이면 일단 true로 맞추는 게 안전
    private CategoryType category;

    @Column(nullable = false)
    private double originalPricePerBaseUnit;

    @Column(nullable = false)
    private double stock;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder.Default
    @Column(name = "current_base_qty", nullable = false)
    private double currentBaseQty = 0;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public void update(String name, String description, Unit unit, Double originalPrice,
                       Double stock, String imageUrl, Store store , CategoryType category) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (unit != null) this.unit = unit;
        if (originalPrice != null) this.originalPricePerBaseUnit = originalPrice;
        if (stock != null) this.stock = stock;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (store != null) this.store = store;
        if (category != null) this.category = category;

    }

    public void patch(Double originalPrice, String imageUrl) {
        if (originalPrice != null) this.originalPricePerBaseUnit = originalPrice;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }

    public void increaseCurrentBaseQty(double qtyBase) {
        if (qtyBase <= 0) {
            throw new IllegalArgumentException("qtyBase must be > 0");
        }
        double next = this.currentBaseQty + qtyBase;
        if (next > this.stock) {
            // 공통 예외체계가 있다면 BusinessException(ExceptionType.STOCK_OVERFLOW)로 교체
            throw new IllegalStateException("재고 초과: 요청=" + qtyBase + ", 남은=" + (this.stock - this.currentBaseQty));
        }
        this.currentBaseQty = next;
    }
}