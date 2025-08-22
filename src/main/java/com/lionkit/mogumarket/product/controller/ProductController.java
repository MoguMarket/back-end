package com.lionkit.mogumarket.product.controller;
import com.lionkit.mogumarket.product.dto.ProductSaveRequest;
import com.lionkit.mogumarket.product.dto.request.ProductUpdateRequest;
import com.lionkit.mogumarket.product.dto.response.ProductGroupBuyOverviewResponse;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.service.ProductQueryService;
import com.lionkit.mogumarket.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import com.lionkit.mogumarket.product.dto.request.ProductSaveRequest;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.service.ProductService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Products", description = "상품 CRUD 및 조회 API")
public class ProductController {

    private final ProductService productService;
    private final ProductQueryService productQueryService;


    // 상품 등록
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "상품 등록",
            description = "상품을 신규로 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Long.class),
                            examples = @ExampleObject(name = "createdId", value = "101"))),
            @ApiResponse(responseCode = "400", description = "요청 바디 유효성 오류"),
            @ApiResponse(responseCode = "404", description = "가게(스토어) 미존재 등 참조 무결성 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Long> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "상품 등록 요청 바디",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductSaveRequest.class),
                            examples = @ExampleObject(name = "예시",
                                    value = """
                                            {
                                              "storeId": 1,
                                              "name": "제주 당근 5kg",
                                              "description": "달달한 봄 당근",
                                              "unit": "KG",
                                              "originalPrice": 3200,
                                              "stock": 500,
                                              "imageUrl": "https://img.cdn/carrot.jpg"
                                            }
                                            """)
                    )
            )
            @RequestBody ProductSaveRequest request
    ) {
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
    @Operation(
            summary = "상품 단건 조회",
            description = "상품 ID로 단건 조회합니다."
    )
    @Parameters({
            @Parameter(name = "id", description = "상품 ID", required = true, example = "101")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductResponse(id));
    }

    // 목록 조회 (페이징)
    @GetMapping
    @Operation(
            summary = "상품 목록(페이징)",
            description = "상품 목록을 페이지 단위로 조회합니다."
    )
    @Parameters({
            @Parameter(name = "page", description = "페이지(0-base)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "10")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))),
    })
    public ResponseEntity<Page<ProductResponse>> listProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(productService.list(page, size));
    }

    // 상품 전체 수정
    @PutMapping("/{id}")
    @Operation(
            summary = "상품 수정",
            description = "상품의 상세 정보를 수정합니다. null인 필드는 수정하지 않습니다."
    )
    @Parameters({
            @Parameter(name = "id", description = "상품 ID", required = true, example = "101")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "400", description = "바디 유효성 오류")
    })
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "상품 수정 요청 바디(부분 수정 허용)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductUpdateRequest.class),
                            examples = @ExampleObject(name = "예시",
                                    value = """
                                            {
                                              "name": "제주 당근 10kg",
                                              "description": "더 달달한 여름 당근",
                                              "unit": "KG",
                                              "originalPrice": 3000,
                                              "stock": 700,
                                              "imageUrl": "https://img.cdn/carrot_v2.jpg",
                                              "storeId": 1
                                            }
                                            """)
                    )
            )
            @RequestBody ProductUpdateRequest request
    ) {
        productService.updateProduct(id, request);
        return ResponseEntity.noContent().build();
    }

    // 상품 삭제
    @DeleteMapping("/{id}")

    @Operation(
            summary = "상품 삭제",
            description = "상품을 삭제합니다."
    )
    @Parameters({
            @Parameter(name = "id", description = "상품 ID", required = true, example = "101")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })

    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{productId}/overview")
    @Operation(
            summary = "상품 + 공구 현황 조회",
            description = "특정 상품의 정보와 현재 진행 중인 공구 상태(단계별 할인, 목표 달성 현황)를 함께 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공적으로 상품 + 공구 정보 반환",
            content = @Content(schema = @Schema(implementation = ProductGroupBuyOverviewResponse.class))
    )
    public ResponseEntity<ProductGroupBuyOverviewResponse> getOverview(
            @Parameter(description = "조회할 상품 ID", required = true, example = "101")
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(productQueryService.getOverview(productId));
    }
}