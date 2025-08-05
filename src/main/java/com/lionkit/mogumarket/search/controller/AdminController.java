package com.lionkit.mogumarket.search.controller;

import com.lionkit.mogumarket.search.schedular.ProductSyncScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "관리자 API", description = "관리자 전용 API elasticsearch 상품 데이터 강제 동기화")
public class AdminController {

    private final ProductSyncScheduler productSyncScheduler;

    @PostMapping("/sync-products")
    @Operation(
            summary = "상품 데이터 Elasticsearch 동기화",
            description = "관리자 전용 API로, 수동으로 상품 데이터를 Elasticsearch에 동기화합니다."
    )
    public ResponseEntity<Void> syncProductsToES() { // 관리자용 API: 수동으로 상품 데이터를 Elasticsearch에 동기화
        productSyncScheduler.syncToES(); // 수동으로 동기화 실행
        return ResponseEntity.ok().build();
    }
}