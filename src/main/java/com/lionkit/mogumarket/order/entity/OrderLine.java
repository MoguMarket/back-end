package com.lionkit.mogumarket.order.entity;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.order.enums.OrderStatus;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.user.entity.User;
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_line_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;


    /** 사용자가 예약한 UNIT 단위 수량  */
    private Double orderedBaseQty;


    /** 스냅샷: 예약 당시 공구 단계, 할인, 단가.
     * stage 의 수정에 따른 데이터 불일치 방지 차원에서,
     * 단순히 stage 를 저장하지 않고 해당 stage 의 세부적인 값 자체를 저장
     */
    private int levelSnapshot;           // 예: 1,2,3...
    private double discountPercentSnapshot; // 예: 15
    private double unitPriceSnapshot;        // 기준단위당 적용 단가


}
