package com.lionkit.mogumarket.product.controller;

import com.lionkit.mogumarket.product.dto.request.ProductSaveRequest;
import com.lionkit.mogumarket.product.dto.request.ProductUpdateRequest;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductService productService;

    // 상품 등록
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "상품 등록")
    @ApiResponse(responseCode = "201", description = "생성 성공",
            content = @Content(schema = @Schema(implementation = Long.class),
                    examples = @ExampleObject(value = "101")))
    public ResponseEntity<Long> createProduct(@RequestBody ProductSaveRequest request) {
        Long productId = productService.saveProduct(request);
        return ResponseEntity.status(201).body(productId);
    }

    // 단건 조회
    @GetMapping("/{id}")
    @Operation(summary = "상품 단건 조회")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductResponse(id));
    }

    // 목록 조회 (페이징)
    @GetMapping
    @Operation(summary = "상품 목록(페이징)")
    public ResponseEntity<Page<ProductResponse>> listProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(productService.list(page, size));
    }

    // 상품 전체 수정
    @PutMapping("/{id}")
    @Operation(summary = "상품 수정")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request
    ) {
        productService.updateProduct(id, request);
        return ResponseEntity.noContent().build();
    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "상품 삭제")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}