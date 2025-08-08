package com.lionkit.mogumarket.product.dto;

import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.product.enums.GroupPurchaseStatus;
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

    private Integer originalPrice;
    private Integer discountPrice;

    private Integer stock;
    private Integer targetCount;

    private LocalDateTime deadline;
    private String imageUrl;

    private CategoryType category;

    private Store store; // 또는 storeId를 쓰는 경우 Long storeId;

    // status는 보통 기본값(WAITING)으로 설정하므로 요청에서 받을 필요 없음
}