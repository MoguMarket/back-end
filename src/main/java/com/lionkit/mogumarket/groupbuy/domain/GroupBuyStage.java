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
@Table(name = "group_buy_stage")
public class GroupBuyStage extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id", nullable = false)
    private GroupBuy groupBuy;

    /** 단계 번호(1,2,3…) */
    @Column(nullable = false)
    private int level;

    /** 해당 단계 시작 수량(기준단위) */
    @Column(nullable = false)
    private double startQty;

    /** 해당 단계의 할인율(%) */
    @Column(nullable = false)
    private double discountPercent;

    /** 해당 단계의 할인율 적용 가격 ( 공구 개설 이후 원가/할인율 수정 불가 )*/
    @Column(nullable = false)
    private long appliedUnitPrice;

    /**
     * appliedUnitPrice 를 구하는데 사용하는 util 메서드
     */
    public void computeAppliedPriceFromSnapshot() {
        double base = groupBuy.getBasePricePerBaseUnitSnapshot();
        this.appliedUnitPrice = Math.round(base * ((100.0 - this.discountPercent) / 100.0));
    }


}