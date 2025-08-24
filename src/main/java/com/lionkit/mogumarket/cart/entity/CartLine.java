package com.lionkit.mogumarket.cart.entity;

import com.lionkit.mogumarket.cart.enums.PurchaseRoute;
import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * 주의: CartLine은 개념적으로 cart–product의 "중간테이블" 역할을 합니다.
 * 다만 수량 등 도메인 속성을 가진 "라인 엔티티"이므로 조인테이블식 이름(CartProduct)이 아닌
 * 이 논리적 이유로 이름을 CartProduct가 아니라 CartLine으로 명명했습니다.
 */
@Entity
@Table(
        name = "cart_line",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_line_cart_product_route",
                columnNames = {"cart_id", "product_id", "route"}
        )
        // UNIQUE(cart_id, product_id)로 WHERE cart_id=?도 커버되므로 추가 인덱스는 생략합니다.
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_line_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** UNIT 기준 수량입니다.  */
    @Column
    private double orderedBaseQty;

    /** 공구 참여 여부(=구매 경로) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private PurchaseRoute route = PurchaseRoute.NORMAL;

    /**
     * cartline 생성 시점이후 해당 가격에 대한 변동이 일어날 수 있기에
     * unitPrice 는 따로 저장하지 않습니다. (cart 에 담긴 product 의 가격은 구매 확정 이전까지 변동 가능)
     * 가격은 항상 ‘지금 시점’으로 다시 계산해서 보여줘야 합니다.
     */


    public void increase(double delta) {
        this.orderedBaseQty = this.orderedBaseQty + delta;
        if (this.orderedBaseQty <= 0) throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
    }

    public void change(double qtyBase) {
        if (qtyBase <= 0) throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        this.orderedBaseQty = qtyBase;
    }

    public void updateCart(Cart cart){
        this.cart = cart;
    }

}
