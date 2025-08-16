// CartSummaryResponse.java
package com.lionkit.mogumarket.cart.dto.reaponse;

import java.util.List;

public record CartSummaryResponse(
        List<CartItemResponse> items,
        int totalQuantity,
        int totalAmount
) {}