package com.lionkit.mogumarket.product.controller;
import com.lionkit.mogumarket.product.dto.request.ProductSaveRequest;
import com.lionkit.mogumarket.product.dto.ProductUpdateDto;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.service.ProductWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Products", description = "상품 CRUD 및 조회 API")
public class ProductWriteController {

    private final ProductWriteService productWriteService;


    // 상품 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상품 등록", description = "상품을 신규로 등록합니다. JSON 메타데이터와 이미지 파일을 multipart로 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "요청 유효성 오류"),
            @ApiResponse(responseCode = "404", description = "가게(스토어) 미존재 등 참조 무결성 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Long> createProduct(
            @RequestPart("request") ProductSaveRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        Long productId = productWriteService.saveProduct(request, image);
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
        return ResponseEntity.ok(productWriteService.getProductResponse(id));
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
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long marketId

    ) {

        return ResponseEntity.ok(productWriteService.list(page, size,marketId));
    }

    // 상품 전체 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상품 수정", description = "상품 상세를 수정합니다. JSON 메타데이터와 이미지 파일을 multipart로 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "400", description = "바디 유효성 오류")
    })
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id,
            @RequestPart("request") ProductUpdateDto request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        productWriteService.updateProduct(id, request, image);
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
        productWriteService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

}
