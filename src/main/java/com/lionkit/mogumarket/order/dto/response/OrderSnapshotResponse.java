package com.lionkit.mogumarket.order.dto.response;

import com.lionkit.mogumarket.order.entity.Orders;
import com.lionkit.mogumarket.order.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 스냅샷 응답 DTO
 */
public record OrderSnapshotResponse(
        Long orderId,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderLineResponse> lines
) {
    public static OrderSnapshotResponse from(Orders orders) {
        return new OrderSnapshotResponse(
                orders.getId(),
                orders.getStatus(),
                orders.getCreatedAt(),
                orders.getLines().stream()
                        .map(OrderLineResponse::from)
                        .toList()
        );
    }
}

