package com.lionkit.mogumarket.cart.dto.request;

import com.lionkit.mogumarket.cart.enums.PurchaseRoute;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 라인 단건 추가/수정 요청 DTO
 * - qtyBase == 0 이면 삭제 정책을 적용할 수도 있음(서비스 로직에서 처리)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartLineUpsertRequest {


    @NotNull
    private Long productId;

    /** 0 이상(0이면 삭제로 간주할 수 있음) */
    @PositiveOrZero
    private double qtyBase;

    /** NORMAL(즉시구매) or GROUP_BUY(공구참여) */
    @NotNull
    private PurchaseRoute route;
}