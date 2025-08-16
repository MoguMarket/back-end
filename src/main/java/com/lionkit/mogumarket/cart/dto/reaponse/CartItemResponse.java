package com.lionkit.mogumarket.cart.dto.reaponse;

public record CartItemResponse(
        Long productId,
        String productName,
        long unitPrice,
        Integer quantity,
        long lineTotal // = unitPrice * quantity
) {}