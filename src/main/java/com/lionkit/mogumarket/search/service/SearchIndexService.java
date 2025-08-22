package com.lionkit.mogumarket.search.service;

import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.search.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class SearchIndexService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductRepository productRepository; // join fetch용

    /** 상품 1건 인덱싱 (store/market까지 안전 로딩) */
    @Transactional(readOnly = true)
    public void indexProduct(Long productId) {
        // store & market join fetch로 LAZY 문제 방지
        Product p = productRepository.findWithStoreAndMarketById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + productId));

        var store  = p.getStore();                 // null 가능
        var market = (store != null) ? store.getMarket() : null;

        long updatedAtMillis = (p.getModifiedAt() != null
                ? p.getModifiedAt()
                : p.getCreatedAt()
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        ProductDocument doc = ProductDocument.builder()
                .id(String.valueOf(p.getId()))                     // ES 문서 id 고정
                .productId(p.getId())
                .storeId(store  != null ? store.getId()  : null)
                .marketId(market != null ? market.getId() : null)
                .name(p.getName())
                .description(p.getDescription())
                .updatedAtMillis(updatedAtMillis)
                .build();

        elasticsearchOperations.save(doc);
    }

    /** 엔티티가 이미 로딩되어 넘어오는 경우 */
    @Transactional(readOnly = true)
    public void indexProduct(Product p) {
        var store  = p.getStore();
        var market = (store != null) ? store.getMarket() : null;

        long updatedAtMillis = (p.getModifiedAt() != null
                ? p.getModifiedAt()
                : p.getCreatedAt()
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        ProductDocument doc = ProductDocument.builder()
                .id(String.valueOf(p.getId()))
                .productId(p.getId())
                .storeId(store  != null ? store.getId()  : null)
                .marketId(market != null ? market.getId() : null)
                .name(p.getName())
                .description(p.getDescription())
                .updatedAtMillis(updatedAtMillis)
                .build();

        elasticsearchOperations.save(doc);
    }

    /** 삭제 동기화(선택) */
    public void deleteProductDoc(Long productId) {
        elasticsearchOperations.delete(String.valueOf(productId), ProductDocument.class);
    }
}