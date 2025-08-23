// com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository
package com.lionkit.mogumarket.groupbuy.repository;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStatus;
import com.lionkit.mogumarket.product.entity.Product;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {
    List<GroupBuy> findByStatus(GroupBuyStatus status);

    // 마감 스케줄링용: endAt 이전인데 아직 OPEN
    List<GroupBuy> findByStatusAndEndAtBefore(GroupBuyStatus status, LocalDateTime time);
    // GroupBuyRepository
    Optional<GroupBuy> findTopByProductAndStatusOrderByCreatedAtDesc(Product product, GroupBuyStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GroupBuy g where g.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    Optional<GroupBuy> findForUpdate(@Param("id") Long id);

    Page<GroupBuy> findByStatusAndEndAtAfter(GroupBuyStatus status, LocalDateTime now, Pageable pageable);
}