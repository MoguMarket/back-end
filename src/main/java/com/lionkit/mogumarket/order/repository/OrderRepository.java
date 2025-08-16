package com.lionkit.mogumarket.order.repository;

import com.lionkit.mogumarket.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, Long> {
}
