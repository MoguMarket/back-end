package com.lionkit.mogumarket.product.dto;


import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.product.enums.Unit;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDto {
    private String name;
    private String description;
    private Unit unit;
    private Double originalPricePerBaseUnit; // null이면 변경 안함
    private Double stock;         // 총 재고 변경 시
    private String imageUrl;
    private CategoryType category;
}
