package com.lionkit.mogumarket.product.dto.response;

import com.lionkit.mogumarket.product.entity.Product;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private double originalPricePerBaseUnit;
    private double stock;
    private Long storeID; // 매핑된 store ID
    private double rating;   // 평균 별점
    private LocalDateTime createdAt;

    public static ProductResponse fromEntity(Product product) {
        double avgRating = product.getReviews().isEmpty()
                ? 0.0
                : product.getReviews().stream()
                .mapToDouble(r -> r.getRating() == null ? 0 : r.getRating())
                .average()
                .orElse(0.0);

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .originalPricePerBaseUnit(product.getOriginalPricePerBaseUnit())
                .stock(product.getStock())
                .storeID(product.getStore().getId())
                .rating(avgRating)
                .createdAt(product.getCreatedAt())
                .build();
    }
}