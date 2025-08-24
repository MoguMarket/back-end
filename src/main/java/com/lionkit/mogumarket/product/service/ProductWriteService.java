package com.lionkit.mogumarket.product.service;


import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.product.dto.request.ProductPatchRequest;
import com.lionkit.mogumarket.product.dto.request.ProductSaveRequest;
import com.lionkit.mogumarket.product.dto.ProductUpdateDto;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.store.entity.Store;
import com.lionkit.mogumarket.store.repsitory.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;


@Service
@RequiredArgsConstructor
public class ProductWriteService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    // CREATE
    @Transactional
    public Long saveProduct(ProductSaveRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new BusinessException(ExceptionType.STORE_NOT_FOUND));


        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .unit(request.getUnit())
                .originalPricePerBaseUnit(request.getOriginalPrice())
                .stock(request.getStock())
                .unit(request.getUnit())
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
                .orElseThrow(() ->new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));
        return ProductResponse.fromEntity(product);
    }

    // READ: 목록
    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Integer page, Integer size, Long marketId) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size < 1 || size > 100) ? 10 : size;

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));

        Page<Product> products;
        if (marketId != null) {
            products = productRepository.findByStore_Market_Id(marketId, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(ProductResponse::fromEntity); // from(Product) 정적 메서드 가정
    }

    // UPDATE: 전체 수정
    @Transactional
    public void updateProduct(Long id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));
        product.update(dto);
    }

    // PATCH: 부분 수정
    @Transactional
    public void patchProduct(Long id, ProductPatchRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));

        product.patch(
                request.getOriginalPrice(),
                request.getImageUrl()
        );
    }

    // DELETE
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
    }


}
