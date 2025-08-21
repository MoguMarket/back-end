package com.lionkit.mogumarket.groupbuy.domain;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBuyStage extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id", nullable = false)
    private GroupBuy groupBuy;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private double startQty;

    @Column(nullable = false)
    private double discountPercent;
}