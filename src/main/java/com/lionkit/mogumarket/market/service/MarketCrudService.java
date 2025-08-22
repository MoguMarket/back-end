package com.lionkit.mogumarket.market.service;

import com.lionkit.mogumarket.market.entity.Market;
import com.lionkit.mogumarket.market.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketCrudService {

    private final MarketRepository marketRepository;

    @Transactional
    public void updateLocation(Long id, Double lat, Double lon) {
        Market m = marketRepository.findById(id).orElseThrow();
        // setter 쓰는 방식(간단)
        m = Market.builder()
                .id(m.getId())
                .marketCode(m.getMarketCode())
                .name(m.getName())
                .landAddress(m.getLandAddress())
                .roadAddress(m.getRoadAddress())
                .sido(m.getSido())
                .sigungu(m.getSigungu())
                .latitude(lat)
                .longitude(lon)
                .distance(m.getDistance())
                .description(m.getDescription())
                .stores(m.getStores())
                .build();
        marketRepository.save(m);
    }

    @Transactional
    public void updateDistance(Long id, int distance) {
        Market m = marketRepository.findById(id).orElseThrow();
        m = Market.builder()
                .id(m.getId())
                .marketCode(m.getMarketCode())
                .name(m.getName())
                .landAddress(m.getLandAddress())
                .roadAddress(m.getRoadAddress())
                .sido(m.getSido())
                .sigungu(m.getSigungu())
                .latitude(m.getLatitude())
                .longitude(m.getLongitude())
                .distance(distance)
                .description(m.getDescription())
                .stores(m.getStores())
                .build();
        marketRepository.save(m);
    }

    public Page<Market> findByRegion(String sido, String sigungu, Pageable pageable) {
        return marketRepository.findBySidoContainingAndSigunguContaining(
                sido == null ? "" : sido,
                sigungu == null ? "" : sigungu,
                pageable
        );
    }

    public Page<Market> findByDistanceRange(int min, int max, Pageable pageable) {
        return marketRepository.findByDistanceBetween(min, max, pageable);
    }
}