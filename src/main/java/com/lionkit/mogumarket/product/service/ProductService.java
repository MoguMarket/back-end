package com.lionkit.mogumarket.product.service;

import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.product.dto.request.ProductPatchRequest;
import com.lionkit.mogumarket.product.dto.request.ProductSaveRequest;
import com.lionkit.mogumarket.product.dto.request.ProductUpdateRequest;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.store.entity.Store;
import com.lionkit.mogumarket.store.repsitory.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ← 이걸로!

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    // CREATE
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
                .imageUrl(request.getImageUrl())
                .store(store)
                .category(CategoryType.valueOf(request.getCategory()))
                .build();

        return productRepository.save(product).getId();
    }

    // READ: 단건
    @Transactional(readOnly = true)
    public ProductResponse getProductResponse(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return ProductResponse.fromEntity(product);
    }

    // READ: 목록
    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 10 : size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return productRepository.findAll(pageable).map(ProductResponse::fromEntity);
    }

    // UPDATE: 전체 수정
    @Transactional
    public void updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        product.update(
                request.getName(),
                request.getDescription(),
                request.getUnit(),
                request.getOriginalPrice(),
                request.getStock(),
                request.getImageUrl(),
                request.getStoreId() != null ?
                        storeRepository.findById(request.getStoreId())
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Store ID"))
                        : null,
                request.getCategory()

        );
    }

    // PATCH: 부분 수정
    @Transactional
    public void patchProduct(Long id, ProductPatchRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        product.patch(
                request.getOriginalPrice(),
                request.getImageUrl()
        );
    }

    // DELETE
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        productRepository.delete(product);
    }
}