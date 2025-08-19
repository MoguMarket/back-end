package com.lionkit.mogumarket.product.service;

import com.lionkit.mogumarket.product.dto.ProductSaveRequest;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.enums.Unit;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.search.document.ProductDocument;
import com.lionkit.mogumarket.search.repository.ProductSearchRepository;
import com.lionkit.mogumarket.search.service.RedisSearchRankService;
import com.lionkit.mogumarket.store.entity.Store;
import com.lionkit.mogumarket.store.repsitory.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    private final ProductSearchRepository productSearchRepository;
    private final RedisSearchRankService redisSearchRankService;

    @Transactional
    public Long saveProduct(ProductSaveRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Store ID"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .unit(request.getUnit())
                .originalPricePerBaseUnit(request.getOriginalPrice())
                .stock(request.getStock())
                .currentBaseQty(0) // 새 상품은 판매 수량 0
                .deadline(request.getDeadline())
                .imageUrl(request.getImageUrl())
                .store(store)
                .build();

        Product saved = productRepository.save(product);

        // Elasticsearch 문서 저장
        ProductDocument doc = ProductDocument.builder()
                .id(UUID.randomUUID().toString())
                .name(saved.getName())
                .description(saved.getDescription())
                .build();
        productSearchRepository.save(doc);

        // 검색 키워드 랭킹 반영
        redisSearchRankService.increaseKeywordScore(saved.getName());

        return saved.getId();
    }
}