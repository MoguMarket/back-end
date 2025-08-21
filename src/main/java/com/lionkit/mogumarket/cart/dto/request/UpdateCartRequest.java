package com.lionkit.mogumarket.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartRequest(
        @NotNull @Min(1) Integer quantity
) {}

