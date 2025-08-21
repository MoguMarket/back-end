package com.lionkit.mogumarket.point.repository;

import com.lionkit.mogumarket.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}