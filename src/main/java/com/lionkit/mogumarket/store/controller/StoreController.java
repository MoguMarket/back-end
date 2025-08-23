package com.lionkit.mogumarket.store.controller;



import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.store.dto.request.StoreSaveRequest;
import com.lionkit.mogumarket.store.dto.response.StoreResponse;
import com.lionkit.mogumarket.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
@Tag(name = "스토어 API", description = "스토어 등록/조회 API")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @Operation(summary = "스토어 등록")
    public ResponseEntity<Long> create(@RequestBody StoreSaveRequest request) {
        Long id = storeService.create(request);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "스토어 단건 조회")
    public ResponseEntity<StoreResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.get(id));
    }

    @GetMapping
    @Operation(summary = "스토어 목록 조회(페이징, marketId 필터 가능)")
    public ResponseEntity<Page<StoreResponse>> list(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) Long marketId
    ) {
        return ResponseEntity.ok(storeService.list(page, size, marketId));
    }

    @GetMapping("/products/{id}")
    @Operation(
            summary = "스토어의 상품 목록 조회",
            description = "스토어 ID로 상품 목록을 페이지 단위로 반환합니다."
    )
    @Parameters({
            @Parameter(name = "id", description = "스토어 ID", example = "1", required = true),
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "10")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "스토어 없음")
    })
    public ResponseEntity<Page<ProductResponse>> getProductsByStoreId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(storeService.getProductsByStoreId(id, page, size));
    }
}
