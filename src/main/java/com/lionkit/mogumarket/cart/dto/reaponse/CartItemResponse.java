package com.lionkit.mogumarket.cart.dto.reaponse;

public record CartItemResponse(
        Long productId,
        String productName,
        Integer unitPrice,
        Integer quantity,
        Integer lineTotal // = unitPrice * quantity
) {}