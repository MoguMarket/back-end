package com.lionkit.mogumarket.order.dto.response;
import com.lionkit.mogumarket.order.entity.OrderLine;
import lombok.Getter;
import lombok.Setter;

/**
 * 주문 라인 응답 DTO
 */

public record OrderLineResponse(
        Long id,
        Long productId,
        Double orderedBaseQty,
        int levelSnapshot,
        double discountPercentSnapshot,
        double unitPriceSnapshot,
        int finalLevelSnapshot,
        double finalDiscountPercentSnapshot,
        double finalUnitPriceSnapshot,
        Long groupBuyId
) {
    public static OrderLineResponse from(OrderLine line) {
        return new OrderLineResponse(
                line.getId(),
                line.getProduct().getId(),
                line.getOrderedBaseQty(),
                line.getLevelSnapshot(),
                line.getDiscountPercentSnapshot(),
                line.getUnitPriceSnapshot(),
                line.getFinalLevelSnapshot(),
                line.getFinalDiscountPercentSnapshot(),
                line.getFinalUnitPriceSnapshot(),
                line.getGroupBuy() != null ? line.getGroupBuy().getId() : null
        );
    }
}


