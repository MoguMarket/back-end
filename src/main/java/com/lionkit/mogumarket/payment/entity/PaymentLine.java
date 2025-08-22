package com.lionkit.mogumarket.payment.entity;


import com.lionkit.mogumarket.order.entity.OrderLine;
import com.lionkit.mogumarket.payment.enums.RefundType;
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


    /**
     * 환불 금액
     */
    @Column(nullable = false)
    @Builder.Default
    private Long refundedAmount = 0L;






}
