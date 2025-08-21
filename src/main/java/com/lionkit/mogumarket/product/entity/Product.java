package com.lionkit.mogumarket.product.entity;

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

    @Column(nullable = false)
    private double originalPricePerBaseUnit;

    @Column(nullable = false)
    private double stock;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public void update(String name, String description, Unit unit, Double originalPrice,
                       Double stock, String imageUrl, Store store) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (unit != null) this.unit = unit;
        if (originalPrice != null) this.originalPricePerBaseUnit = originalPrice;
        if (stock != null) this.stock = stock;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (store != null) this.store = store;
    }

    public void patch(Double originalPrice, String imageUrl) {
        if (originalPrice != null) this.originalPricePerBaseUnit = originalPrice;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }
}