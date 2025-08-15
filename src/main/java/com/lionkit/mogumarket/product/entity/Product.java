package com.lionkit.mogumarket.product.entity;
import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.product.enums.Unit;
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    /** 노출/주문 단위 (KG, L, EA 등) **/
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;


    /**
     * 단위(BaseUnit) 당 원가.
     * 할인가의 경우 ProductStage 의 discountPercent 를 활용하여 직접 구하도록 설계
     */
    @Column(nullable = false)
    private double originalPricePerBaseUnit; // 원가


    /**
     * 전부 '기준단위'(g/ml/ea) 기준
     */
    @Column(nullable = false)
    private double stock;            // (등록 당시의) 총 재고. 수량.
    @Column(nullable = false)
    private double currentBaseQty;   // 현재 누적 구매 수량


    private LocalDateTime deadline; // 공구 모집 마감일
    private String imageUrl;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;


    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductStage> stages = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();



    /** 낙관적 락(기본 방어막) */
    @Version
    private Long version;


    /**
     * 누적 구매 수량 증가
     */
    public void increaseCurrentBaseQty(double delta) {
        if (delta <= 0) throw new BusinessException(ExceptionType.INVALID_QTY);

        double nextBaseQty = this.currentBaseQty + delta;

        if (nextBaseQty > this.stock ) throw new BusinessException(ExceptionType.STOCK_OVERFLOW);

        this.currentBaseQty = nextBaseQty;
    }

    /** 잔여 재고 조회(편의) */
    public double getRemainingStock() {
        return this.stock - this.currentBaseQty;
    }


}
