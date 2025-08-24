package com.lionkit.mogumarket.user.entity;


import com.lionkit.mogumarket.cart.entity.Cart;
import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.order.entity.Orders;
import com.lionkit.mogumarket.point.entity.Point;
import com.lionkit.mogumarket.review.entity.Review;
import com.lionkit.mogumarket.security.jwt.enums.Role;
import com.lionkit.mogumarket.security.jwt.entity.RefreshToken;
import com.lionkit.mogumarket.security.oauth2.enums.ProviderType;
import com.lionkit.mogumarket.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User extends BaseEntity {

    /**
     * 우리 애플리케이션 상의 (물리적) 식별자값
     * 자체로그인, OAuth2 공통
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * OAuth2 provider 벤더명 (KAKAO, NAVER, GOOGLE)
     * 자체 로그인일 경우 null
     */
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    /**
     * OAuth2 provider 상의 식별자 값
     * 자체 로그인일 경우 null
     */
    private String providerId;



    /**
     * 자체로그인 논리적 식별자값
     * OAuth2 로그인일경우 null
     */
    @Column(
            unique = true)
    private String username;

    /**
     * 자체로그인 비밀번호
     * OAuth2 로그인일경우 null
     */
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 리프레시 토큰
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();


    /**
     * 부가적인 정보들
     */
    // 닉네임. 사용자명
    private String nickname;
    private String email;
    private String address;
    private String phone;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Store store;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Point point;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cart cart;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();





    public void updateRole(Role role){
        this.role = role;
    }



}
