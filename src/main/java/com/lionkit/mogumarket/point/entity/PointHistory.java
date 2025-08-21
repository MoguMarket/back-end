package com.lionkit.mogumarket.point.entity;
import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.payment.entity.Payment;
import com.lionkit.mogumarket.payment.entity.PaymentHistory;
import com.lionkit.mogumarket.point.enums.PointEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory extends BaseEntity {

    @Version
    private Long version;


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_id", nullable=false)
    private Point point;


    /**
     * 포인트 변화를 일으킨 event 의 종류
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=24)
    private PointEventType type;

    /**
     * point 변화를 일으킨 관련 결제 이력의 정보.
     * 결제와 관련한 변화가 아니면 null 을 허용합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)  // 결제 관련이 아니면 null 허용
    @JoinColumn(name = "payment_history_id")
    private PaymentHistory paymentHistory;


    /**
     * 해당 포인트 이벤트에 의한 포인트 변화량.
     */
    @Column(name = "volume", nullable = false)
    private Integer volume;



    /**
     * 이벤트 전 사용 가능한 총 잔액 스냅샷 ( 검증용 )
     */
    @Column(name = "before_balance_snapshot")
    private Integer beforeBalanceSnapshot;


    /**
     * 이벤트 이후 사용 가능한 총 잔액 스냅샷 ( 검증용 )
     */
    @Column(name = "after_balance_snapshot")
    private Integer afterBalanceSnapshot;



    /**
     * 멱등키로, 중복 작업 요청을 방지합니다.
     */
    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;








}