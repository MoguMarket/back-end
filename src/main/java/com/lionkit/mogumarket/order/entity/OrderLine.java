package com.lionkit.mogumarket.order.entity;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주의: OrderLine은 개념적으로 orders–product의 "중간테이블" 역할을 합니다.
 * 다만 주문 시점의 수량/단가/할인/단계 등의 스냅샷을 보존해야 하므로
 * 이름을 OrderProduct가 아니라 OrderLine으로 명명했습니다.
 */
@Entity
@Table(name = "order_line")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_line_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** 사용자가 예약한 UNIT 단위 수량 */
    @Column(nullable = false)
    private Double orderedBaseQty;

    /**
     * 스냅샷: 예약 당시 공구 단계/할인/단가
     * stage 변경에 따른 데이터 불일치 방지를 위해,
     * 단순히 stage 참조 대신 당시의 값을 저장합니다.
     */
    private int levelSnapshot;                    // 예: 1, 2, 3 ...
    private double discountPercentSnapshot;       // 예: 15
    private double unitPriceSnapshot;             // 기준단위당 적용 단가

    /**
     * 스냅샷: 공구 종료 시점의 단계/할인/단가
     * (종료 시점 정산 기준이 필요할 때 사용)
     */
    private int finalLevelSnapshot;               // 예: 1, 2, 3 ...
    private double finalDiscountPercentSnapshot;  // 예: 15
    private double finalUnitPriceSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id") // nullable=true: 일반 주문은 null
    private GroupBuy groupBuy;


    /** 편의 메서드 */
    // updateOrders 만 실수로 하고 (OrderLine 의) addLine 을 하지 않는 것을 방지하는 차원에서 외부 패키지에서 접근 못하도록 protected 설정
    protected void updateOrders(Orders orders) {
        this.orders = orders;
    }


    public void finalizeSnapshots(int level, double discount, double unitPrice) {
        this.finalLevelSnapshot = level;
        this.finalDiscountPercentSnapshot = discount;
        this.finalUnitPriceSnapshot = unitPrice;
    }


}