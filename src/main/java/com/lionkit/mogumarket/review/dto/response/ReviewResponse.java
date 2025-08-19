package com.lionkit.mogumarket.review.dto.response;

import com.lionkit.mogumarket.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {
    private Long id;
    private Long productId;
    private Long userId;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ReviewResponse from(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct().getId())
                .userId(r.getUser().getId())
                .rating(r.getRating())
                .createdAt(r.getCreatedAt())
                .modifiedAt(r.getModifiedAt())
                .build();
    }
}