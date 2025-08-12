package com.lionkit.mogumarket.purchase.entity;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.purchase.enums.PurchaseStatus;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "purchase_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    /** 사용자가 예약한 기준단위 수량 (g/ml/ea) */
    private Double orderedBaseQty;

    /** 구매 상태  */
    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;


    /** 스냅샷: 예약 당시 공구 단계, 할인, 단가.
     * stage 의 수정에 따른 데이터 불일치 방지 차원에서,
     * 단순히 stage 를 저장하지 않고 해당 stage 의 세부적인 값 자체를 저장
     */
    private int levelSnapshot;           // 예: 1,2,3...
    private double discountPercentSnapshot; // 예: 15
    private double unitPriceSnapshot;        // 기준단위당 적용 단가


}
