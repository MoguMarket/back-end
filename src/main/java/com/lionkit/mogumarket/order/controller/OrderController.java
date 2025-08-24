package com.lionkit.mogumarket.order.controller;

import com.lionkit.mogumarket.order.dto.request.ConfirmMultiOrderRequest;
import com.lionkit.mogumarket.order.dto.request.CreateOrderLineRequest;
import com.lionkit.mogumarket.order.dto.response.OrderLineResponse;
import com.lionkit.mogumarket.order.dto.response.OrderSnapshotResponse;
import com.lionkit.mogumarket.order.service.OrderService;
import com.lionkit.mogumarket.security.oauth2.principal.PrincipalDetails;
import com.lionkit.mogumarket.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "주문 API", description = "주문 확정/조회/공구 스냅샷/결제 실패 롤백")
@Validated
public class OrderController {

    private final OrderService orderService;

    // ---------- 1) 단일 상품 주문 확정 ----------
    @PostMapping("/confirm-single")
    @Operation(
            summary = "단일 상품 주문 확정",
            description = "단일 상품에 대해 즉시구매 또는 공구참여로 주문을 확정합니다. 성공 시 생성된 ordersId를 반환합니다."
    )
    public ResponseEntity<OrderSnapshotResponse> confirmSingleProductOrder(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody CreateOrderLineRequest  req
    ) {
        Long ordersId = orderService.confirmSingleProductOrder(
                principal.getUser().getId(),
                req
        );
        OrderSnapshotResponse snapshot = orderService.getOrderSnapshot(principal.getUser().getId(), ordersId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(snapshot);
    }

    @PostMapping("/confirm-multi")
    @Operation(summary = "다건 상품 주문 확정",
            description = "여러 OrderLine을 한 번에 확정하고, 주문/라인 스냅샷을 반환합니다.")
    public ResponseEntity<OrderSnapshotResponse> confirmMultiProductOrder(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody ConfirmMultiOrderRequest req
    ) {
        Long userId = principal.getUser().getId();

        List<CreateOrderLineRequest> commands = req.lines().stream()
                .map(l -> new CreateOrderLineRequest(l.productId(), l.qtyBase(), l.participateInGroupBuy()))
                .toList();

        Long ordersId = orderService.confirmMultiProductOrder(userId, commands);
        OrderSnapshotResponse snapshot = orderService.getOrderSnapshot(userId, ordersId);
        return ResponseEntity.status(HttpStatus.CREATED).body(snapshot);
    }


    // ---------- 2) 공구 종료 시 라인 최종 스냅샷 확정 ----------
    @PostMapping("/group-buys/{groupBuyId}/finalize-snapshots")
    @Operation(
            summary = "공구 종료 스냅샷 확정",
            description = "해당 groupBuyId에 참여한 주문 라인들의 최종 단계/할인/단가 스냅샷을 확정합니다. " +
                    "보통 GroupBuyService.closeGroupBuy(...) 직후 호출합니다."
    )
    public ResponseEntity<Void> finalizeSnapshotsForGroupBuy(
            @PathVariable Long groupBuyId
    ) {
        orderService.finalizeSnapshotsForGroupBuy(groupBuyId);
        return ResponseEntity.noContent().build();
    }

    // ---------- 3) 결제 실패 롤백 ----------
    @PostMapping("/{ordersId}/rollback")
    @Operation(
            summary = "결제 실패 롤백",
            description = "결제 실패 시 주문의 라인별 재고/공구 누적을 되돌리고 주문 상태를 FAILED로 전환합니다. 멱등 처리됩니다."
    )
    public ResponseEntity<Void> rollbackStocks(
            @PathVariable Long ordersId
    ) {
        orderService.rollbackStocks(ordersId);
        return ResponseEntity.noContent().build();
    }

    // ---------- 4) 주문 스냅샷(라인 포함) 조회 ----------
    @GetMapping("/{ordersId}")
    @Operation(
            summary = "주문 스냅샷 조회",
            description = "지정한 주문의 스냅샷을 조회합니다. 응답에는 해당 주문의 주문라인(OrderLine)들이 포함됩니다."
    )
    public ResponseEntity<OrderSnapshotResponse> getOrderSnapshot(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long ordersId
    ) {
        return ResponseEntity.ok(orderService.getOrderSnapshot(principal.getUser().getId(), ordersId));
    }

    // ---------- 5) 나의 주문 목록(각 주문에 라인 포함) ----------
    @GetMapping
    @Operation(
            summary = "나의 주문 목록",
            description = "로그인 사용자의 주문 목록(최신순)을 반환합니다. 각 주문 객체에는 주문라인 목록이 함께 포함됩니다."
    )
    public ResponseEntity<List<OrderSnapshotResponse>> listMyOrders(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        return ResponseEntity.ok(orderService.listMyOrders(principal.getUser().getId()));
    }

    // ---------- 6) 특정 주문의 라인 목록 ----------
    @GetMapping("/{ordersId}/lines")
    @Operation(
            summary = "주문 라인 목록",
            description = "특정 주문(ordersId)의 모든 주문라인을 반환합니다."
    )
    public ResponseEntity<List<OrderLineResponse>> listLinesByOrders(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long ordersId
    ) {
        return ResponseEntity.ok(orderService.listLinesByOrders(principal.getUser().getId(), ordersId));
    }

    // ---------- 7) 주문 라인 단건 스냅샷 ----------
    @GetMapping("/lines/{orderLineId}")
    @Operation(
            summary = "주문 라인 스냅샷",
            description = "특정 주문 라인(orderLineId)의 스냅샷을 반환합니다."
    )
    public ResponseEntity<OrderLineResponse> getOrderLineSnapshot(
            @AuthenticationPrincipal PrincipalDetails loginedUser,
            @PathVariable Long orderLineId
    ) {
        return ResponseEntity.ok(orderService.getOrderLineSnapshot(loginedUser.getUser().getId(), orderLineId));
    }
}
