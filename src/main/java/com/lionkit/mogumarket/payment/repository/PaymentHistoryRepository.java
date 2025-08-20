package com.lionkit.mogumarket.payment.repository;

import com.lionkit.mogumarket.payment.entity.PaymentHistory;
import com.lionkit.mogumarket.payment.enums.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    Optional<PaymentHistory> findByProviderAndProviderTransactionId(PaymentProvider provider, String providerTransactionId);
}