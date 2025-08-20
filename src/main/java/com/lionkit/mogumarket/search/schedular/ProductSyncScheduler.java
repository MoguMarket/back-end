package com.lionkit.mogumarket.search.schedular;

import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.search.document.ProductDocument;
import com.lionkit.mogumarket.search.repository.ProductDocumentRepository;
import com.lionkit.mogumarket.search.service.SyncCheckpointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSyncScheduler {

    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository; // ES 리포지토리
    private final SyncCheckpointService checkpointService;

    @Scheduled(cron = "${product.sync.cron:0 0 * * * *}")
    public void syncToES() {
        try {
            // 0) 인덱스가 비어 있으면 1회 전량 색인(부트스트랩)
            if (productDocumentRepository.count() == 0) {
                log.info("[Bootstrap] ES index is empty. Reindexing all products...");
                List<Product> all = productRepository.findAll();
                saveAllToES(all);
                // 체크포인트를 all의 max(modifiedAt)로 설정
                LocalDateTime newCp = maxModifiedAt(all, checkpointService.loadLastSyncedAt());
                checkpointService.saveLastSyncedAt(newCp);
                log.info("[Bootstrap] Indexed {} products", all.size());
                return;
            }

            // 1) 마지막 체크포인트 로드(없으면 아주 과거)
            LocalDateTime lastSynced = checkpointService.loadLastSyncedAt();
            log.debug("Last synced at: {}", lastSynced);

            // 2) 증분: '>' 권장 (경계 중복/유실 방지)
            List<Product> changed = productRepository.findByModifiedAtGreaterThan(lastSynced);
            if (changed.isEmpty()) {
                log.info("No products to sync");
                return;
            }
            log.info("Found {} products to sync", changed.size());

            // 3) 색인
            saveAllToES(changed);

            // 4) 처리 배치의 max(modifiedAt)로 체크포인트 갱신
            LocalDateTime newCheckpoint = maxModifiedAt(changed, lastSynced);
            checkpointService.saveLastSyncedAt(newCheckpoint);
            log.info("Successfully synced {} products to Elasticsearch. New checkpoint: {}",
                    changed.size(), newCheckpoint);

        } catch (Exception e) {
            log.error("Failed to sync products to Elasticsearch. Will retry in next cycle.", e);
        }
    }

    private void saveAllToES(List<Product> products) {
        List<ProductDocument> docs = products.stream()
                .map(p -> ProductDocument.builder()
                        .id(p.getId().toString())
                        .productId(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .updatedAt(
                                p.getModifiedAt() != null
                                        ? p.getModifiedAt().atZone(ZoneId.systemDefault()).toInstant()
                                        : Instant.now()
                        )
                        .build())
                .toList();
        productDocumentRepository.saveAll(docs);
    }

    private static LocalDateTime maxModifiedAt(List<Product> batch, LocalDateTime fallback) {
        return batch.stream()
                .map(Product::getModifiedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(fallback);
    }
}