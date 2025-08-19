package com.lionkit.mogumarket.store.controller;



import com.lionkit.mogumarket.store.dto.request.StoreSaveRequest;
import com.lionkit.mogumarket.store.dto.response.StoreResponse;
import com.lionkit.mogumarket.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
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
}