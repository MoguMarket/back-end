package com.lionkit.mogumarket.product.dto.response;

import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.entity.ProductStage;
import com.lionkit.mogumarket.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private double originalPricePerBaseUnit; // 원가
    private double discountPercent;           // 현재 적용 할인율
    private double sales;                     // 누적 판매량
    private double rating;                    // 평균 별점

    private LocalDateTime deadline;
    private LocalDateTime createdAt;

    public static ProductResponse fromEntity(Product product) {
        //  현재 진행중인 할인 단계(예시: level 1)
        ProductStage stage = product.getStages().isEmpty() ? null : product.getStages().get(0);

        //  평균 별점 계산
        double avgRating = product.getReviews().isEmpty()
                ? 0.0
                : product.getReviews().stream()
                .mapToDouble(Review::getRating) // Review 엔티티에 getRating() 있다고 가정
                .average()
                .orElse(0.0);

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .originalPricePerBaseUnit(product.getOriginalPricePerBaseUnit())
                .discountPercent(stage != null ? stage.getDiscountPercent() : 0.0)
                .sales(product.getCurrentBaseQty()) // 판매량
                .rating(avgRating)
                .deadline(product.getDeadline())
                .createdAt(product.getCreatedAt())
                .build();
    }
}