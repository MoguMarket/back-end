package com.lionkit.mogumarket.search.schedular;

import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.search.document.ProductDocument;
import com.lionkit.mogumarket.search.repository.ProductSearchRepository;
import com.lionkit.mogumarket.search.service.SyncCheckpointService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductSyncScheduler {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository; // ES 리포지토리
    private final SyncCheckpointService checkpointService;

    // 매시간 0분
    @Scheduled(cron = "${product.sync.cron}")
    public void syncToES() {
        // 1) 쿼리 시작 시각 (체크포인트는 성공 후 이 값으로 갱신)
        LocalDateTime syncStart = LocalDateTime.now();

        // 2) 마지막 동기화 시각 로드
        LocalDateTime last = checkpointService.loadLastSyncedAt();

        // 3) 변경분만 조회
        List<Product> changed = productRepository.findByModifiedAtAfter(last);
        if (changed.isEmpty()) {
            checkpointService.saveLastSyncedAt(syncStart);
            return;
        }

        // 4) ES 문서로 매핑
        List<ProductDocument> docs = changed.stream()
                .map(p -> ProductDocument.builder()
                        .id(p.getId().toString())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .toList();

        // 5) 업서트 저장
        try {
            productSearchRepository.saveAll(docs);
            // 6) 성공했을 때만 체크포인트 갱신
            checkpointService.saveLastSyncedAt(syncStart);
        } catch (Exception e) {
            // ES 장애 등으로 실패하면 체크포인트 갱신하지 않음 → 다음 주기에 재시도
            // 필요시 로그 남기기
            // log.error("Product sync failed", e);
        }
    }
}