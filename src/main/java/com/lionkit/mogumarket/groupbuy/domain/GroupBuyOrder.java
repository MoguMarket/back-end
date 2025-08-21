package com.lionkit.mogumarket.groupbuy.domain;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBuyOrder extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id", nullable = false)
    private GroupBuy groupBuy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private double quantity;
}