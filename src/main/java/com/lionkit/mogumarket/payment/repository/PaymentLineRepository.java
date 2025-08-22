package com.lionkit.mogumarket.payment.repository;

import com.lionkit.mogumarket.payment.entity.PaymentLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentLineRepository extends JpaRepository<PaymentLine, Long> {
    List<PaymentLine> findByPayment_Id(Long paymentId);
    List<PaymentLine> findByOrderLine_Id(Long orderLineId);
}