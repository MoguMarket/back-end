package com.lionkit.mogumarket.review.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewCreateRequest {
    private Long productId;
    private Long userId;   // 실제 운영에서는 인증 토큰에서 꺼내는 걸 권장
    private Integer rating; // 1~5
}