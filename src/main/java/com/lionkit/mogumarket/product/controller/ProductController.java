package com.lionkit.mogumarket.product.controller;

import com.lionkit.mogumarket.product.dto.ProductSaveRequest;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.enums.SortType;
import com.lionkit.mogumarket.product.service.FilterService;
import com.lionkit.mogumarket.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "상품 API", description = "상품 CRUD/목록/정렬 API")
public class ProductController {

    private final ProductService productService;
    private final FilterService filterService;

    // CREATE
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "상품 등록",
            responses = {
                    @ApiResponse(responseCode = "201", description = "생성 성공",
                            content = @Content(schema = @Schema(implementation = Long.class),
                                    examples = @ExampleObject(value = "101")))
            }
    )
    public ResponseEntity<Long> createProduct(@RequestBody ProductSaveRequest request) {
        Long productId = productService.saveProduct(request);
        return ResponseEntity.status(201).body(productId);
    }

    // READ: 단건
    @GetMapping("/{id}")
    @Operation(summary = "상품 단건 조회", responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "상품 ID", example = "101") @PathVariable Long id
    ) {
        return ResponseEntity.ok(productService.getProductResponse(id));
    }

    // READ: 페이징 목록 (기본: 최신순)
    @GetMapping
    @Operation(summary = "상품 목록(페이징)",
            description = "page=0부터 시작, size 1~100. 기본 정렬은 NEWEST(생성일 DESC).")
    public ResponseEntity<Page<ProductResponse>> listProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false, defaultValue = "NEWEST") SortType sort
    ) {
        return ResponseEntity.ok(productService.list(page, size, storeId, sort));
    }

    // READ: 스토어별 목록(페이징)
    @GetMapping("/by-store/{storeId}")
    @Operation(summary = "스토어별 상품 목록(페이징)")
    public ResponseEntity<Page<ProductResponse>> listByStore(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "NEWEST") SortType sort
    ) {
        return ResponseEntity.ok(productService.list(page, size, storeId, sort));
    }


    // UPDATE (전체 수정)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "상품 수정(전체)")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request
    ) {
        productService.updateProduct(id, request);
        return ResponseEntity.noContent().build();
    }

    // PATCH (부분 수정: 예시로 가격/마감일/이미지 등)
    @PatchMapping("/{id}")
    @Operation(summary = "상품 부분 수정")
    public ResponseEntity<Void> patchProduct(
            @PathVariable Long id,
            @RequestBody ProductPatchRequest request
    ) {
        productService.patchProduct(id, request);
        return ResponseEntity.noContent().build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "상품 삭제", responses = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}