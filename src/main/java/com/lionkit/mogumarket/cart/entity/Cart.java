package com.lionkit.mogumarket.cart.entity;

import com.lionkit.mogumarket.product.entity.Product;
import com.test.oauth.global.base.domain.BaseEntity;
import com.test.oauth.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;




    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    public void changeQuantity(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
    }




}
