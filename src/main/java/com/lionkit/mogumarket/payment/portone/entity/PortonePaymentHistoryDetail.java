package com.lionkit.mogumarket.payment.portone.entity;


import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.payment.entity.PaymentHistory;
import com.lionkit.mogumarket.payment.enums.PaymentStatus;
import com.lionkit.mogumarket.payment.portone.enums.PortoneTransactionEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 특정 트랜잭션에 대한 상태 변화(event 발생)를 추적하기 위한 엔티티로,
 * 해당 트랜젝션의 상태 변화 마다 생성됩니다.
 */
@Entity
@Table(name = "portone_payment_history_detail")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortonePaymentHistoryDetail extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * 어떤 트랜잭션에 대한 변화인지 나타냅니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_history_id", nullable = false)
    private PaymentHistory paymentHistory;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 48)
    private PortoneTransactionEventType transactionEventType;


    /**
     * 포트원 상에서의 해당 트랜잭션의 상태 변화 시점을 저장합니다.
     * 포트원 상의 statusChangedAt 필드값을 의미합니다.
     *  updatedAt  ⊇ statusChangedAt 이기에 트랜잭션 단위의 statusChangedAt만 저장합니다.
     *  ( updatedAt 은 결제 전반에 대한 것이라 트랜잭션이 아닌 결제 단위로 변경됩니다. 즉, 결제에 대한 모든 트랜잭션의 변화가 일어날 때마다 업데이트 됩니다 )
     * LocalDateTime 이 아닌 Instant 를 통해 타임존 정보까지 저장합니다.
     */
    private Instant transactionStatusChangedAt;


    /**
     * 결제 (부분) 취소 event 발생시 포트원에서 발급되는 cancellationId 입니다.
     * 결제 취소 관련 event 가 아닌 트랜젝션의 경우 nullable.
     */
    @Column(length = 100)
    private String cancellationId;

    /**
     * 포트원에 기록된 결제 취소 사유
     */
    @Column(length = 200)
    private String reason;

    @Transient
    public PaymentStatus toPaymentStatus() {
        return transactionEventType.toPaymentStatus();
    }
}
