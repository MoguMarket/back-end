package com.lionkit.mogumarket.product.service;

import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.enums.SortType;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterService {

        /**
         * 상품 필터링 서비스
         * SortType에 따라 상품을 정렬하여 반환합니다.
         */
    private final ProductRepository productRepository;

        public List<ProductResponse> getProducts(SortType sortType) {
        Sort sort = Sort.by(sortType.getDirection(), sortType.getField());
        List<Product> products = productRepository.findAll(sort);

        return products.stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }
}