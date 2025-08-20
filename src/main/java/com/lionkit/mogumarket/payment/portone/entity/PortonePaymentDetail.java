package com.lionkit.mogumarket.payment.portone.entity;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.payment.entity.Payment;
import com.lionkit.mogumarket.payment.enums.PortonePaymentMethod;
import com.lionkit.mogumarket.payment.portone.enums.PortonePgVendor;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "portone_payment_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortonePaymentDetail extends BaseEntity {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Payment 와 일대일 대응되며,
     * PortonePaymentDetail 이 연관관계의 주인이 됩니다.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment;


    /**
     * 포트원 상 결제 방식 ( 카드, 계좌거래 , .. ).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PortonePaymentMethod portonePaymentMethod;


    /**
     * 포트원 상에서 관리자 콘솔 단위로 발급되는 식별자인 storeId 에 대응됩니다.
     */
    @Column(nullable = false, length = 100)
    private String storeId;


    /**
     * 포트원 상 결제 진행한 pg사(채널)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private PortonePgVendor pgVendor;


    /**
     * 결제 서비스 제공 업체 상으로 결제 request 가 접수된, 즉 결제 객체 생성 시점.
     * portone 상에서는 requestAt 필드를 의미합니다.
     * LocalDateTime 이 아닌 Instant 를 통해 타임존 정보까지 저장합니다.
     */
    private Instant paymentRequestedAt;




}
