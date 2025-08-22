package com.lionkit.mogumarket.point.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PointRedeemRequestDto {

    @NotNull
    @Positive
    Long amount;

    /**
     * 같은 처리에 대한 중복 요청에 대한 중복 처리를 방지하기 위한 멱등키로,
     * 프론트에서 요청 1건당 UUIDv4 같은 랜덤키를 만들어 요청하면 됩니다.
     */
    @NotBlank
    String idempotencyKey;

}
