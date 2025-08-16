package com.lionkit.mogumarket.market.controller;

import com.lionkit.mogumarket.market.service.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market")
@Tag(name = "시장 API", description = "시장 관련 API")
public class MarketController {

    private final MarketService marketService;

    /**
     * 상품 목록을 페이징 처리하여 조회합니다.
     *
     * @param page    페이지 번호 (기본값: 1)
     * @param perPage 페이지당 상품 수 (기본값: 10, 최대: 100)
     * @param sido    시도 필터 (예: 경상북도)
     * @param sigungu 시군구 필터 (예: 구미시)
     * @return 상품 목록과 페이징 정보
     */
    @GetMapping
    @Operation(
            summary = "시장 목록 조회",
            description = "시장을 페이징 처리하여 조회합니다. \n" +
                    "시도(sido)와 시군구(sigungu)로 필터링할 수 있습니다."
    )
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String sigungu
    ) {
        if (page < 1) page = 1;
        if (perPage < 1) perPage = 10;
        if (perPage > 100) perPage = 100;

        Map<String, Object> body = marketService
                .fetch(page, perPage, sido, sigungu, null) // 시장명 LIKE는 null
                .block();

        return ResponseEntity.ok(body);
    }
}