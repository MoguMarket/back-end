package com.lionkit.mogumarket.payment.entity;


import com.lionkit.mogumarket.order.entity.OrderLine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주의: PaymentLine은 개념적으로 orderline–payment의 "중간테이블" 역할을 합니다.
 */
@Entity
@Table(name = "payment_line")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentLine {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_line_id", nullable = false)
    private OrderLine orderLine;

    /** 배분된 현금/포인트 금액(원). 합계는 Payment·Orders와 일치해야 함 */
    @Column(nullable = false) private Long cashAmount;
    @Column(nullable = false) private Long pointAmount;

    /** 환불 누적치 — 부분환불 다회 발생 시 트래킹 */
    @Column(nullable = false) @Builder.Default private Long refundedCash = 0L;
    @Column(nullable = false) @Builder.Default private Long refundedPoint = 0L;


}
