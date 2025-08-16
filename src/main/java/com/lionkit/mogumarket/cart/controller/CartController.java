package com.lionkit.mogumarket.cart.controller;

import com.lionkit.mogumarket.cart.dto.request.AddCartRequest;
import com.lionkit.mogumarket.cart.dto.reaponse.CartItemResponse;
import com.lionkit.mogumarket.cart.dto.request.UpdateCartRequest;
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

    @PostMapping
    @Operation(summary = "장바구니에 상품 추가", description = "사용자의 장바구니에 상품을 추가합니다.")
    public ResponseEntity<Void> add(
            @Valid @RequestBody AddCartRequest req,
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        cartService.add(userId, req.productId(), req.quantity());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(
            summary = "장바구니 목록 조회",
            description = "사용자의 장바구니에 담긴 상품 목록을 조회합니다."
    )
    public ResponseEntity<List<CartItemResponse>> list(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(cartService.list(userId));
    }

    @PatchMapping("/{productId}")
    @Operation(
            summary = "장바구니 상품 수량 업데이트",
            description = "장바구니에 담긴 특정 상품의 수량을 업데이트합니다."
    )
    public ResponseEntity<Void> updateQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartRequest req,
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        cartService.updateQuantity(userId, productId, req.quantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    @Operation(
            summary = "장바구니에서 상품 제거",
            description = "장바구니에서 특정 상품을 제거합니다."
    )
    public ResponseEntity<Void> remove(
            @PathVariable Long productId,
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        cartService.remove(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(
            summary = "장바구니 비우기",
            description = "사용자의 장바구니를 비웁니다."
    )
    public ResponseEntity<Void> clear(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        cartService.clear(userId);
        return ResponseEntity.noContent().build();
    }
}