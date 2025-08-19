package com.lionkit.mogumarket.payment.entity;


import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.order.entity.Orders;
import com.lionkit.mogumarket.payment.enums.CurrencyCode;
import com.lionkit.mogumarket.payment.enums.PaymentProvider;
import com.lionkit.mogumarket.payment.enums.PaymentStatus;
import com.lionkit.mogumarket.payment.portone.entity.PortonePaymentDetail;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment",
        /**
         * 다른 provider가 같은 ID를 쓰는 경우 충돌 방지를 위해 unique 제약 조건을 겁니다.
        */
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_provider_payment_id",
                columnNames = {"paymentProvider","providerPaymentId"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    /**
     * 우리 서비스 내부 식별자
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * provider 상의 식별자
     */
    @Column(nullable = false, length = 100)
    private String providerPaymentId;

    /**
     * 결제 서비스 제공 업체
     * PORTONE 혹은 OPEN_BANKING
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider paymentProvider;



    /**
     * 현재 결제 상태 스냅샷.
     * provider 상의 자체 status 가 아닌, 우리 애플리케이션 상의 상태값.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus currentPaymentStatus;

    /**
     * 결제 금액
     */
    @Column(nullable = false)
    private Long amount;

    /**
     * 화폐 단위
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;


    /**
     *
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;


    @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private PortonePaymentDetail portonePaymentDetail;


    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PaymentHistory> paymentHistories = new ArrayList<>();


    // 편의 메서드

    public void attachPortoneDetail(PortonePaymentDetail detail) {
        detail.setPayment(this);
        this.portonePaymentDetail = detail;
    }

    public void addPaymentHistory(PaymentHistory history) {
        history.setPayment(this);
        this.paymentHistories.add(history);
    }

}
