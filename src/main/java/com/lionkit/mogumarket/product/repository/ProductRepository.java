package com.lionkit.mogumarket.product.repository;

import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param; // ← 이걸 쓰세요 (중요)

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 증분 동기화: LAZY 터지지 않도록 연관을 미리 로딩
    @EntityGraph(attributePaths = {"store", "store.market"})
    List<Product> findByModifiedAtGreaterThan(LocalDateTime from);

    // 전량 동기화(부트스트랩)용: findAll을 대체하는 파생 메서드
    @EntityGraph(attributePaths = {"store", "store.market"})
    List<Product> findAllBy(); // ← select * from product 와 동일하게 동작

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    Optional<Product> findForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    Optional<Product> findForUpdateNoWait(@Param("id") Long id);

    List<Product> findByModifiedAtAfter(LocalDateTime lastSyncTime);

    Page<Product> findByCategoryOrderByCreatedAtDesc(CategoryType category, Pageable pageable);
    Page<Product> findByCategoryInOrderByCreatedAtDesc(List<CategoryType> categories, Pageable pageable);
    Page<Product> findByOriginalPricePerBaseUnitBetweenOrderByCreatedAtDesc(double min, double max, Pageable pageable);

    @Query("""
      select p
      from Product p
      join fetch p.store s
      join fetch s.market
      where p.id = :id
    """)
    Optional<Product> findWithStoreAndMarketById(@Param("id") Long id);

    Page<Product> findByStoreId(Long storeId, Pageable pageable);
    Page<Product> findByStore_Market_Id(Long marketId, Pageable pageable);
}
