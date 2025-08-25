package com.lionkit.mogumarket.groupbuy.domain;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.groupbuy.enums.GroupBuyStatus;
import com.lionkit.mogumarket.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "group_buy")
public class GroupBuy extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false ,unique = true)
    private Product product;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Column(nullable = false)
    private double targetQty;  // 목표 수량. 공동구매 전용 재고.

    @Column(nullable = false)
    @Builder.Default
    private double currentQty= 0d; // 현재 수량. 공구 누적만 의미. (전체 누적은 product 의 currentBaseQty)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupBuyStatus status= GroupBuyStatus.OPEN;

    @Column(nullable = false)
    private double maxDiscountPercent;

    /** 개설 시점 ‘기준단위당 원가’ 스냅샷(이후 불변) */
    @Column(nullable = false)
    private double basePricePerBaseUnitSnapshot;


    @Version
    private Long version;


    @Builder.Default
    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyStage> stages = new ArrayList<>();


    /**
     * 참가=수량 증가 (취소 불가 정책이기에 감소 메서드는 제공하지 않음)
     * @param qty
     */
    public void increaseQty(double qty) {
        this.currentQty += qty;
        if (this.currentQty >= targetQty) {
            this.status = GroupBuyStatus.SUCCESS;
        }
    }

    public void decreaseQty(double qty) {
        if (qty <= 0) throw new BusinessException(ExceptionType.INVALID_QTY);

        this.currentQty = Math.max(0, this.currentQty - qty);

        // 해당 결제건에 의해 공구 마감될 예정이었다면, 공구 마감이 안되게 되돌림
        if (this.currentQty < targetQty && this.getStatus() == GroupBuyStatus.SUCCESS) {
            this.status = GroupBuyStatus.OPEN;
        }
    }


    public void close() {
        this.status = GroupBuyStatus.CLOSED;
    }
}