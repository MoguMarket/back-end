package com.lionkit.mogumarket.search.schedular;

import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.search.document.ProductDocument;
import com.lionkit.mogumarket.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductSyncScheduler {

    private final ProductRepository productRepository;

    @Nullable
    private final ProductSearchRepository productSearchRepository;

    @Scheduled(cron = "0 0 * * * *") // 매시간 실행
    public void syncToES() {
        List<Product> products = productRepository.findAll();
        List<ProductDocument> docs = products.stream().map(p -> ProductDocument.builder()
                .id(UUID.randomUUID().toString())
                .name(p.getName())
                .description(p.getDescription())
                .build()).toList();
        productSearchRepository.saveAll(docs);
    }
}