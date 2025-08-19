package com.lionkit.mogumarket.payment.entity;


import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.payment.enums.PaymentProvider;
import com.lionkit.mogumarket.payment.portone.entity.PortonePaymentHistoryDetail;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 특정 결제 건(payment)에 대해 업체에서 생성한 트랜젝션을 저장합니다.
 * payment_history 는 트랜잭션 최초 생성 시 한 번만 생성되며,
 * 해당 트랜젝션(payment_history)에 대한 상태 변화는 payment_history_detail 을 통해 상태 변화마다 기록합니다.
 */
@Entity
@Table(
        name = "payment_history",
        /**
         * 멱등, 중복 방지용 unique 선언
        */
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_history_provider_tx",
                columnNames = {"provider", "providerTransactionId"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistory extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 업체 상 해당 transaction 에 발급한 식별자
     */
    @Column(nullable = false, length = 100)
    private String providerTransactionId;


    /**
     * 특정 결제건에 대한 트랜젝션임을 나타냅니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    /**
     * 결제 요청 받은 업체와 실제 처리하는 업체가 다를 경우를 대비하여
     * Payment 와 별개로 트랜잭션에 대해서도 provider 를 저장합니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider provider;



    /**
     * 해당 트랜잭션이 발생한 업체가 포트원일 경우
     * PortonePaymentHistoryDetail 을 통해 상태 추적을 수행합니다.
     */
    @OneToMany(mappedBy = "paymentHistory", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PortonePaymentHistoryDetail> portonePaymentHistoryDetails = new ArrayList<>();

    // TODO : OPEN_BANKING 에 대한 detail enity 와의 관계 추가


    // 편의 메서드
    public void addPortonePaymentHistoryDetail(PortonePaymentHistoryDetail detail) {
        detail.setPaymentHistory(this);
        this.portonePaymentHistoryDetails.add(detail);
    }


}
