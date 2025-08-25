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
import com.lionkit.mogumarket.s3.service.S3Service;
import com.lionkit.mogumarket.store.entity.Store;
import com.lionkit.mogumarket.store.repsitory.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


@Service
@RequiredArgsConstructor
public class ProductWriteService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final S3Service s3Service; // S3 서비스 주입

    // CREATE
    // CREATE (multipart)
    @Transactional
    public Long saveProduct(ProductSaveRequest request, MultipartFile image) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new BusinessException(ExceptionType.STORE_NOT_FOUND));

        String imageUrl = request.getImageUrl(); // 호환성 유지
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.uploadImage(image); // 업로드 후 URL 반환
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .unit(request.getUnit())
                .originalPricePerBaseUnit(request.getOriginalPrice())
                .stock(request.getStock())
                .imageUrl(imageUrl)
                .store(store)
                .category(CategoryType.valueOf(request.getCategory()))
                .build();

        return productRepository.save(product).getId();
    }

    @Transactional
    public Long saveProduct(ProductSaveRequest request) {
        return saveProduct(request, null);
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
    public void updateProduct(Long id, ProductUpdateDto dto, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));

        // 1) DTO로 필드 업데이트
        product.update(dto);

        // 2) 이미지 파일이 오면 업로드 후 URL 교체
        if (image != null && !image.isEmpty()) {
            String newUrl = s3Service.uploadImage(image);
            product.patch(null, newUrl); // originalPrice는 그대로, imageUrl만 교체
        }
        // 이미지가 없고 dto.getImageUrl()가 제공됐다면 product.update(dto)에서 이미 반영됨
        // 필요 시, dto.imageUrl를 무시하고 파일만 허용하려면 여기서 덮어쓰기 정책을 정하면 됨
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
