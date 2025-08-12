package com.lionkit.mogumarket.purchase.repository;

import com.lionkit.mogumarket.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}
