package com.lionkit.mogumarket.order.repository;

import com.lionkit.mogumarket.order.entity.Orders;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    @EntityGraph(attributePaths = { "lines" })
    Optional<Orders> findById(Long id);

    @EntityGraph(attributePaths = { "lines" })
    List<Orders> findAllByUserIdOrderByCreatedAtDesc(Long userId);

}
