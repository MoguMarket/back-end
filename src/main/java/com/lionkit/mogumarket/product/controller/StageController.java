package com.lionkit.mogumarket.product.controller;

import com.lionkit.mogumarket.product.dto.response.StageResponse;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.entity.ProductStage;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.product.service.ProductStageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/products/{productId}/stages", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "상품 단계 API", description = "공동구매 단계(현재/다음/잔여/적용단가) 조회 API")
public class StageController {

    private final ProductStageService productStageService;
    private final ProductRepository productRepository;

    @GetMapping("/current")
    @Operation(
            summary = "현재 단계 조회",
            description = "현재 누적 구매 수량에 따라 적용 중인 상품의 단계 정보를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = StageResponse.class),
                                    examples = @ExampleObject(
                                            name = "예시",
                                            value = """
                                                    {
                                                      "level": 2,
                                                      "startBaseQty": 1000.0,
                                                      "discountPercent": 10.0
                                                    }
                                                    """
                                    ))),
                    @ApiResponse(responseCode = "404", description = "상품 또는 단계 없음")
            }
    )
    public ResponseEntity<StageResponse> getCurrentStage(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        ProductStage current = productStageService.getCurrentStage(product);
        return ResponseEntity.ok(StageResponse.fromEntity(current));
    }

    @GetMapping("/next")
    @Operation(
            summary = "다음 단계 조회",
            description = "현재 단계 다음에 도달할 상품 단계 정보를 반환합니다. 없으면 null 필드가 될 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = StageResponse.class),
                                    examples = @ExampleObject(
                                            name = "예시",
                                            value = """
                                                    {
                                                      "level": 3,
                                                      "startBaseQty": 2000.0,
                                                      "discountPercent": 15.0
                                                    }
                                                    """
                                    ))),
                    @ApiResponse(responseCode = "404", description = "상품 없음")
            }
    )
    public ResponseEntity<StageResponse> getNextStage(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        ProductStage next = productStageService.getNextStage(product);
        return ResponseEntity.ok(StageResponse.fromEntity(next));
    }

    @GetMapping("/remaining")
    @Operation(
            summary = "다음 단계까지 남은 수량",
            description = "현재 누적 구매 수량 기준으로 다음 단계까지 필요한 추가 수량을 반환합니다. 다음 단계가 없으면 null 입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = Double.class),
                                    examples = @ExampleObject(name = "예시", value = "350.0"))),
                    @ApiResponse(responseCode = "404", description = "상품 없음")
            }
    )
    public ResponseEntity<Double> getRemainingToNext(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        Double remaining = productStageService.remainingToNext(product);
        return ResponseEntity.ok(remaining);
    }

    @GetMapping("/price")
    @Operation(
            summary = "현재 단계 기준 적용 단가",
            description = "현재 단계의 할인율을 적용한 기준 단가(원)를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = Double.class),
                                    examples = @ExampleObject(name = "예시", value = "8900.0"))),
                    @ApiResponse(responseCode = "404", description = "상품 없음")
            }
    )
    public ResponseEntity<Double> getAppliedUnitPrice(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        double price = productStageService.getAppliedUnitPrice(product);
        return ResponseEntity.ok(price);
    }
}