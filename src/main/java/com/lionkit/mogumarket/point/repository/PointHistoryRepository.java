package com.lionkit.mogumarket.point.repository;

import com.lionkit.mogumarket.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    Optional<PointHistory> findByIdempotencyKey(String idempotencyKey);
}