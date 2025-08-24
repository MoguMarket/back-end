package com.lionkit.mogumarket.product.controller;

import com.lionkit.mogumarket.category.enums.CategoryType;
import com.lionkit.mogumarket.product.dto.response.ProductOverviewResponse;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.enums.SortType;
import com.lionkit.mogumarket.product.service.ProductReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductReadController {

    private final ProductReadService productReadService;

    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "NEWEST") SortType sort
    ) {
        return ResponseEntity.ok(productReadService.filterProducts(sort));
    }

    @GetMapping("/{productId}/overview")
    @Operation(
            summary = "상품 + 공구 현황 조회",
            description = "특정 상품의 정보와 현재 진행 중인 공구 상태(단계별 할인, 목표 달성 현황)를 함께 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공적으로 상품 + 공구 정보 반환",
            content = @Content(schema = @Schema(implementation = ProductOverviewResponse.class))
    )
    public ResponseEntity<ProductOverviewResponse> getOverview(
            @Parameter(description = "조회할 상품 ID", required = true, example = "101")
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(productReadService.getOverview(productId));
    }
    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 상품 목록")
    public ResponseEntity<Page<ProductResponse>> listByCategoryPath(
            @PathVariable CategoryType category,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(productReadService.listByCategory(category, page, size));
    }

    @GetMapping("/by-categories")
    @Operation(summary = "여러 카테고리 상품 목록")
    public ResponseEntity<Page<ProductResponse>> listByCategories(
            @RequestParam List<CategoryType> categories,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(productReadService.listByCategories(categories, page, size));
    }

    // 가격 범위 필터 조회
    @GetMapping("/price")
    @Operation(
            summary = "가격 범위로 상품 조회",
            description = "최소/최대 가격으로 상품을 필터링해 페이지네이션으로 반환합니다. " +
                    "minPrice 또는 maxPrice가 비어있으면 해당 경계는 제한하지 않습니다."
    )
    @Parameters({
            @Parameter(name = "minPrice", description = "최소 가격(포함)", example = "1000"),
            @Parameter(name = "maxPrice", description = "최대 가격(포함)", example = "5000"),
            @Parameter(name = "page",     description = "페이지(0-base)", example = "0"),
            @Parameter(name = "size",     description = "페이지 크기", example = "10")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = org.springframework.data.domain.Page.class)))
    })
    public ResponseEntity<Page<ProductResponse>> listByPrice(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(productReadService.listByPrice(minPrice, maxPrice, page, size));
    }
}
