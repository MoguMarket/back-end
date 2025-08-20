package com.lionkit.mogumarket.payment.repository;

import com.lionkit.mogumarket.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
}