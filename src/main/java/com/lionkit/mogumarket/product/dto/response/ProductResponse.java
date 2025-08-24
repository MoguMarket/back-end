package com.lionkit.mogumarket.product.dto.response;

import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.enums.Unit;
import lombok.*;

import java.time.LocalDateTime;

/**
 * product 엔티티 자체에 대한 정보 reseponse
 */
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
    private String storName; // 카테고리 정보 (추가 필요시)
    private double rating;   // 평균 별점
    private Unit unit; // 단위 (추가 필요시)
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
                .storName(product.getStore().getName())
                .rating(avgRating)
                .unit(product.getUnit())
                .createdAt(product.getCreatedAt())
                .build();
    }
}