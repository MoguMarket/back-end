// CartSummaryResponse.java
package com.lionkit.mogumarket.cart.dto.response;

import com.lionkit.mogumarket.cart.entity.CartLine;
import com.lionkit.mogumarket.product.entity.Product;
import lombok.*;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartSummaryResponse{
        List<CartLineResponse> items;
        double totalQuantity;
        double totalAmount;

        /** 이미 변환된 아이템 리스트로부터 요약 DTO 생성 */
        public static CartSummaryResponse fromItems(List<CartLineResponse> items) {
                double totalQty = items.stream().mapToDouble(CartLineResponse::getQuantity).sum();
                double totalAmt = items.stream().mapToDouble(CartLineResponse::getLineTotal).sum();
                return CartSummaryResponse.builder()
                        .items(items)
                        .totalQuantity(totalQty)
                        .totalAmount(totalAmt)
                        .build();
        }

}