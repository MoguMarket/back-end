package com.lionkit.mogumarket.product.repository;

import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.product.entity.Product;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStoreId(Long storeId, Pageable pageable);


    Page<Product> findByStore_Market_Id(Long marketId, Pageable pageable);

    List<Product> findByModifiedAtGreaterThan(LocalDateTime from);

    /**
     * 공동 구매 참여 == 구매 확정 (환불 불가) ->  Product 행 선점(비관적 락)
     * 비관적 락 + 5초 타임아웃 (데드락 방지 차원: 비관적 락만 적용시 무한대기타게됨 )
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    Optional<Product> findForUpdate(@Param("id") Long id);

    /**
     * 비관적 락 +  대기 없이 즉시 종료 버전
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    Optional<Product> findForUpdateNoWait(@Param("id") Long id);


    Page<Product> findByCategoryOrderByCreatedAtDesc(CategoryType category, Pageable pageable);

    // 다중 카테고리(선택): IN 조건
    Page<Product> findByCategoryInOrderByCreatedAtDesc(List<CategoryType> categories, Pageable pageable);

    Page<Product> findByOriginalPricePerBaseUnitBetweenOrderByCreatedAtDesc(
            double min, double max, Pageable pageable
    );

    @EntityGraph(attributePaths = {"store", "store.market"})
    List<Product> findAllBy(); // ← select * from product 와 동일하게 동작
    @Query("""
      select p
      from Product p
      join fetch p.store s
      join fetch s.market
      where p.id = :id
    """)
    Optional<Product> findWithStoreAndMarketById(@org.springframework.data.repository.query.Param("id") Long id);


}



