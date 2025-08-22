package com.lionkit.mogumarket.groupbuy.domain;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
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
public class GroupBuy extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Column(nullable = false)
    private double targetQty;  // 목표 수량

    @Column(nullable = false)
    private double currentQty; // 현재 수량

    @Enumerated(EnumType.STRING)
    private GroupBuyStatus status;

    @Column(nullable = false)
    private double maxDiscountPercent;

    @Builder.Default
    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyStage> stages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyOrder> orders = new ArrayList<>();

    public void increaseQty(double qty) {
        this.currentQty += qty;
        if (this.currentQty >= targetQty) {
            this.status = GroupBuyStatus.SUCCESS;
        }
    }

    public void close() {
        this.status = GroupBuyStatus.CLOSED;
    }
}