package com.lionkit.mogumarket.cart.controller;

import com.lionkit.mogumarket.cart.dto.request.CartBulkSetRequest;
import com.lionkit.mogumarket.cart.dto.request.CartLineUpsertRequest;
import com.lionkit.mogumarket.cart.dto.response.CartLineResponse;
import com.lionkit.mogumarket.cart.dto.response.CartSummaryResponse;
import com.lionkit.mogumarket.cart.enums.PurchaseRoute;
import com.lionkit.mogumarket.cart.service.CartService;
import com.lionkit.mogumarket.security.oauth2.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
@Tag(name = "장바구니 API", description = "장바구니 관련 API")
public class CartController {

    private final CartService cartService;


    @GetMapping("/lines")
    @Operation(
            summary = "장바구니 라인 목록",
            description = "사용자의 장바구니 라인과 라인별 적용 단가/금액을 조회합니다."
    )
    public ResponseEntity<List<CartLineResponse>> list(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(cartService.list(userId));
    }

    @GetMapping("/summary")
    @Operation(
            summary = "장바구니 요약",
            description = "라인 목록과 총 수량/총 금액 요약 정보를 반환합니다."
    )
    public ResponseEntity<CartSummaryResponse> summary(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(cartService.summary(userId));
    }

    @PostMapping("/lines")
    @Operation(
            summary = "장바구니 라인 단건 추가/수정",
            description = "qtyBase=0 이면 해당 라인을 삭제합니다. 그 외엔 신규 생성 또는 수량 절대값 변경입니다."
    )
    public ResponseEntity<List<CartLineResponse>> upsertLine(
            @Valid @RequestBody CartLineUpsertRequest req,
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(cartService.addOrUpdateCartLine(userId, req));
    }

    @PostMapping("/bulk-set")
    @Operation(
            summary = "장바구니 라인 벌크 세팅",
            description = "여러 라인을 한 번에 추가/수정/삭제합니다. 같은 productId에 대해 route별로 동시 처리 가능합니다."
    )
    public ResponseEntity<List<CartLineResponse>> bulkSet(
            @Valid @RequestBody CartBulkSetRequest req,
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(cartService.bulkSet(userId, req));
    }

    @DeleteMapping("/lines/{productId}")
    @Operation(
            summary = "장바구니 라인 삭제",
            description = "특정 상품/구매경로(route)의 라인을 삭제합니다."
    )
    public ResponseEntity<Void> removeLine(
            @PathVariable Long productId,
            @RequestParam PurchaseRoute route,
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        cartService.remove(userId, productId, route);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(
            summary = "장바구니 비우기",
            description = "사용자의 장바구니를 전체 삭제합니다."
    )
    public ResponseEntity<Void> clear(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        cartService.clear(userId);
        return ResponseEntity.noContent().build();
    }
}