package com.lionkit.mogumarket.product.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 부분 수정 시 사용 예시 (필요 필드만) */
@Getter @Setter
public class ProductPatchRequest {
    private Double originalPrice;   // 가격만 수정
    private String imageUrl;        // 이미지 교체
    private LocalDateTime deadline; // 마감일 변경
}