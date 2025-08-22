package com.lionkit.mogumarket.product.dto.request;


import com.lionkit.mogumarket.product.enums.Unit;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Unit unit;
    private Double originalPrice; // null이면 변경 안함
    private Double stock;         // 총 재고 변경 시
    private String imageUrl;
    private LocalDateTime deadline;
    private Long storeId;         // 소속 스토어 변경 시
}