package com.lionkit.mogumarket.product.entity;
import com.lionkit.mogumarket.cart.entity.Cart;
import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.purchase.entity.Purchase;
import com.lionkit.mogumarket.product.enums.GroupPurchaseStatus;
import com.lionkit.mogumarket.review.entity.Review;
import com.lionkit.mogumarket.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "product_id")
    private Long id;

    private String name;

    @Lob
    private String description;

    private Integer originalPrice; //원가
    private Integer discountPrice; //할인가

    private Integer stock; //재고
    private Integer targetCount; // 목표 공구 인원 수
    private Integer currentCount; // 현재 공구 신청 인원

    private LocalDateTime deadline; // 공구 모집 마감일
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private CategoryType category;

    @Enumerated(EnumType.STRING)
    private GroupPurchaseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Purchase> purchase = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart> carts = new ArrayList<>();
}
