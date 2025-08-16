package com.lionkit.mogumarket.cart.entity;

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
                name = "uk_cart_line_cart_product",
                columnNames = {"cart_id", "product_id"}
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
    @Column(nullable = false)
    private Double orderedBaseQty;
}
