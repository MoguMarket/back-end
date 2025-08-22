package com.lionkit.mogumarket.market.repository;

import com.lionkit.mogumarket.market.entity.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<Market, Long> {
    // 시도/시군구 필터 + 페이징 (정렬은 Pageable의 Sort로)
    Page<Market> findBySidoContainingAndSigunguContaining(
            String sido, String sigungu, Pageable pageable
    );

    // 거리 범위 필터 (예: 0~5000m)
    Page<Market> findByDistanceBetween(int minDistance, int maxDistance, Pageable pageable);


}
