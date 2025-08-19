package com.lionkit.mogumarket.product.dto;

import com.lionkit.mogumarket.product.enums.Unit;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ProductSaveRequest {
    private String name;
    private String description;
    private Unit unit;
    private double originalPrice; // 원가
    private double stock;
    private LocalDateTime deadline;
    private String imageUrl;
    private Long storeId; // store 매핑용
}