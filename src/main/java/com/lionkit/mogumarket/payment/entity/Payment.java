package com.lionkit.mogumarket.payment.entity;


import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.order.entity.Orders;
import com.lionkit.mogumarket.payment.enums.CurrencyCode;
import com.lionkit.mogumarket.payment.enums.PaymentProvider;
import com.lionkit.mogumarket.payment.enums.PaymentStatus;
import com.lionkit.mogumarket.payment.enums.RefundType;
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
     * 결제 서비스 제공 업체
     * PORTONE 혹은 OPEN_BANKING.
     * 포인트로 전액 결제시 Payment 는 null 입니다.
     */
    @Enumerated(EnumType.STRING)
    @Column( length = 20)
    private PaymentProvider paymentProvider;


    /**
     * provider 상의 식별자.
     * 포인트로 전액 결제시 providerPaymentId 는 null 입니다.
     */
    @Column(length = 100)
    private String providerPaymentId;


    /**
     * 현재 결제 상태 스냅샷.
     * provider 상의 자체 status 가 아닌, 우리 애플리케이션 상의 상태값.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus currentPaymentStatus;


    /**
     * 현금(카드/이체 등)으로 실제 청구된 금액.
     * 즉, amount - paidPointAmount.
     */
    @Column(nullable = false)
    private Long paidCashAmount;

    /**
     * 포인트로 차감된 금액(1포인트=1원 가정)
     */
    @Column(nullable = false)
    private Long paidPointAmount;



    /**
     * 총 결제금액(원) .
     * paidCashAmount + paidPointAmount 와 항상 같아야 합니다.
     */
    @Column(nullable = false)
    private Long amount;



    /**
     * 환불 금액에 대한 지급 방식( 포인트 / 현금).
     * 한 payment 내의 orderline 별 환불에 대해서는 한 가지 방식으로 통일하여 환불 청구 가능합니다.
     */
    @Enumerated(EnumType.STRING)
    private RefundType refundType;


    /**
     * 총 환불 결제금액(원) .
     * 한 paymnet 내의 orderline 들에 대해 청구된 총 환불액입니다.
     */
    @Column
    @Builder.Default
    private Long totalRefundAmount= 0L;


    /**
     * 화폐 단위
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;


    /**
     * 어떤 주문건에 대한 것인지
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    /**
     * Payment 동시 환불 방지용 버전락
     */
    @Version
    private Long version;


    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PaymentHistory> paymentHistories = new ArrayList<>();

    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<PaymentLine> lines = new ArrayList<>();


    @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private PortonePaymentDetail portonePaymentDetail;


    /**
     * Payment 금액 무결성 체크
     */
    @PrePersist
    @PreUpdate
    private void validateAmounts() {
        if (paidCashAmount == null || paidPointAmount == null || amount == null)
            throw new IllegalStateException("amount fields must not be null");
        if (paidCashAmount < 0 || paidPointAmount < 0 || amount < 0)
            throw new IllegalStateException("negative amount not allowed");
        if (amount.longValue() != paidCashAmount + paidPointAmount)
            throw new IllegalStateException("amount != cash + point");
    }




    // 편의 메서드

    public void ensureRefundType(RefundType requested) {
        if (this.refundType == null) this.refundType = requested;
        else if (this.refundType != requested)
            throw new IllegalStateException("Refund method must be consistent per payment");
    }

    public void addToTotalRefund(long delta) {
        if (delta < 0) throw new IllegalArgumentException("negative refund");
        if (this.totalRefundAmount + delta > getRefundableAmount())
            throw new IllegalStateException("refund exceeds available");
        this.totalRefundAmount += delta;
    }

    public long getRefundableAmount() {
        return (refundType == RefundType.CASH) ? paidCashAmount : paidPointAmount;
    }

    public void attachPortoneDetail(PortonePaymentDetail detail) {
        detail.setPayment(this);
        this.portonePaymentDetail = detail;
    }

    public void addPaymentHistory(PaymentHistory history) {
        history.setPayment(this);
        this.paymentHistories.add(history);
    }

}
