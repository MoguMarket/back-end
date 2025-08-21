package com.lionkit.mogumarket.product.dto;

import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.product.enums.Unit;
import com.lionkit.mogumarket.store.entity.Store;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSaveRequest{

    private String name;
    private String description;

    private Unit unit;

    private double originalPricePerBaseUnit;
    private double currentBaseQty;

    private Integer stock;
    private Integer targetCount;

    private LocalDateTime deadline;
    private String imageUrl;

    private CategoryType category;

    private Long storeId;

    // status는 보통 기본값(WAITING)으로 설정하므로 요청에서 받을 필요 없음
}