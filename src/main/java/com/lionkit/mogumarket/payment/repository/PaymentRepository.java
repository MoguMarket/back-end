package com.lionkit.mogumarket.payment.repository;

import com.lionkit.mogumarket.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrders_Id(Long ordersId);
    Optional<Payment> findByMerchantUid(String merchantUid);

}