package com.lionkit.mogumarket.point.repository;

import com.lionkit.mogumarket.point.entity.Point;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {

    @Query("select p from Point p where p.user.id = :userId")
    Optional<Point> findByUserId(@Param("userId") Long userId);

    /**
     * 동시 차감/보류 방지를 위해 결제 시에는 행 잠금
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Point p where p.user.id = :userId")
    Optional<Point> findByUserIdForUpdate(@Param("userId") Long userId);
}