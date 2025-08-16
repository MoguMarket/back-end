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

import java.util.ArrayList;
import java.util.List;

/**
 * Orders는 한 번의 결제/확정에 묶인 주문 헤더입니다.
 * Order 라는 이름은 DB 예약어와 충돌할 수 있어 엔티티/테이블 모두 'orders'로 통일했습니다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    /** 주문 상태  */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;


    /**
     * 주문은 삭제 대신 상태전환을 사용하므로 cascade/remove 금지.
     * (DB FK도 ON DELETE RESTRICT/NO ACTION 유지 )
     */
    @Builder.Default
    @OneToMany(mappedBy = "orders")
    private List<OrderLine> lines = new ArrayList<>();

}
