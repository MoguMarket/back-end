package com.lionkit.mogumarket.payment.portone.repository;

import com.lionkit.mogumarket.payment.portone.entity.PortonePaymentHistoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortonePaymentHistoryDetailRepository extends JpaRepository<PortonePaymentHistoryDetail, Long> {
}