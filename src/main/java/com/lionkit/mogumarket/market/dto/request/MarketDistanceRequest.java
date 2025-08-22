package com.lionkit.mogumarket.market.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class MarketDistanceRequest {
    @Min(0)
    private int distance;
}