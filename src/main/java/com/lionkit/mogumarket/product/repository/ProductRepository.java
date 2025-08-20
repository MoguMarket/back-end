package com.lionkit.mogumarket.product.repository;

import com.lionkit.mogumarket.product.entity.Product;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByModifiedAtGreaterThan(LocalDateTime from);


    /** 공동 구매 참여 == 구매 확정 (환불 불가) ->  Product 행 선점(비관적 락)
     * 비관적 락 + 5초 타임아웃 (데드락 방지 차원: 비관적 락만 적용시 무한대기타게됨 )
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    Optional<Product> findForUpdate(@Param("id") Long id);

    /** 비관적 락 +  대기 없이 즉시 종료 버전 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    Optional<Product> findForUpdateNoWait(@Param("id") Long id);

    List<Product>findByModifiedAtAfter(LocalDateTime lastSyncTime);
}