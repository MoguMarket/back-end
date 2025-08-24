package com.lionkit.mogumarket.groupbuy.domain;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "group_buy_user",
        uniqueConstraints = @UniqueConstraint(name="uk_gb_user", columnNames={"group_buy_id","user_id"}))
public class GroupBuyUser extends BaseEntity {


        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="group_buy_id", nullable=false)
        private GroupBuy groupBuy;

        @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
        private User user;

        @Column(nullable=false)
        @Builder.Default
        private double totalQty = 0d;

        private LocalDateTime lastJoinedAt;

        public void addQty(double qty) {
            this.totalQty += qty;
            this.lastJoinedAt = LocalDateTime.now();
        }
    }
