// MarketLocationRequest.java
package com.lionkit.mogumarket.market.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MarketLocationRequest {
    @NotNull private Double latitude;
    @NotNull private Double longitude;
}