package com.lionkit.mogumarket.review.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RatingSummaryResponse {
    private Long productId;
    private Double average; // 소수점 평균 (null이면 0.0로 내려도 됨)
    private Long count;
}