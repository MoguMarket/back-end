package com.lionkit.mogumarket.cart.dto.response;


import com.lionkit.mogumarket.cart.entity.CartLine;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartLineResponse {
        Long productId;
        String productName;
        double unitPrice;
        double quantity;
        double lineTotal; // = unitPrice * quantity


        public static CartLineResponse of(CartLine line, double unitPrice) {
                double qty = line.getOrderedBaseQty();
                return CartLineResponse.builder()
                        .productId(line.getProduct().getId())
                        .productName(line.getProduct().getName())
                        .unitPrice(unitPrice)
                        .quantity(qty)
                        .lineTotal(unitPrice * qty)
                        .build();
        }


}