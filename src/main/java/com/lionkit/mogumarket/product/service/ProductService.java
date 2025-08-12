package com.lionkit.mogumarket.product.service;

import com.lionkit.mogumarket.product.dto.ProductSaveRequest;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.search.document.ProductDocument;
import com.lionkit.mogumarket.search.repository.ProductSearchRepository;
import com.lionkit.mogumarket.search.service.RedisSearchRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Nullable
    private final ProductSearchRepository productSearchRepository;
    private final RedisSearchRankService redisSearchRankService;

    public void saveProduct(ProductSaveRequest request) {
//        Product product = Product.builder()
//                .name(request.getName())
//                .description(request.getDescription())
//                .originalPricePerBaseUnit(request.getOriginalPrice())
//                .discountPricePerBaseUnit(request.getDiscountPrice())
//                .stock(request.getStock())
//                .targetCount(request.getTargetCount())
//                .currentCount(0)
//                .deadline(request.getDeadline())
//                .imageUrl(request.getImageUrl())
//                .category(request.getCategory())
//                .status(GroupPurchaseStage.WAITING)
//                .store(request.getStore())
//                .build();
//
//        productRepository.save(product);
//
//        ProductDocument doc = ProductDocument.builder()
//                .id(UUID.randomUUID().toString())
//                .name(product.getName())
//                .description(product.getDescription())
//                .build();
//        productSearchRepository.save(doc);
//
//        redisSearchRankService.increaseKeywordScore(product.getName());
    }
}

