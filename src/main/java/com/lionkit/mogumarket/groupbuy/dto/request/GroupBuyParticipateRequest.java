package com.lionkit.mogumarket.groupbuy.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GroupBuyParticipateRequest(

        @Schema(description = "공동구매 ID", example = "1")
        @NotNull Long groupBuyId,


        @Schema(description = "참여 수량", example = "3")
        @NotNull @Positive Double qty
) {}
