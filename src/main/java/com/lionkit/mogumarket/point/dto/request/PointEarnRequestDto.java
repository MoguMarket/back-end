package com.lionkit.mogumarket.point.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointEarnRequestDto {

    @NotNull
    @Positive
    Long amount ;

    @NotBlank
    String idempotencyKey;

}
