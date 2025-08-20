package com.lionkit.mogumarket.point.repository;

import com.lionkit.mogumarket.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
}
