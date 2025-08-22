package com.lionkit.mogumarket.market.controller;

import com.lionkit.mogumarket.market.dto.request.MarketDistanceRequest;
import com.lionkit.mogumarket.market.dto.request.MarketLocationRequest;
import com.lionkit.mogumarket.market.dto.response.MarketResponse;
import com.lionkit.mogumarket.market.entity.Market;
import com.lionkit.mogumarket.market.service.MarketCrudService;
import com.lionkit.mogumarket.market.service.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market")
@Tag(name = "시장 API", description = "시장 관련 API")
public class MarketController {

    private final MarketService marketService;
    private final MarketCrudService marketCrudService;

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

    /** DB에서 거리순/필터 조회 */
    @GetMapping("/db")
    @Operation(summary = "DB 시장 목록 조회(정렬/필터)", description = "DB에 저장된 시장을 거리순/지역 필터로 조회합니다.")
    public ResponseEntity<Page<MarketResponse>> listFromDb(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String sigungu,
            @RequestParam(defaultValue = "distance") String sortBy,          // distance, name 등
            @RequestParam(defaultValue = "asc") String direction              // asc/desc
    ) {
        if (page < 1) page = 1;
        if (perPage < 1) perPage = 10;
        if (perPage > 100) perPage = 100;

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page - 1, perPage, sort);

        Page<Market> result = marketCrudService.findByRegion(sido, sigungu, pageable);
        return ResponseEntity.ok(result.map(MarketResponse::from));
    }

    /** 거리 범위 조회 (예: 0~5000m) + 정렬 가능 */
    @GetMapping("/db/by-distance")
    @Operation(summary = "DB 시장 거리 범위 조회", description = "저장된 distance 값으로 범위를 지정해 조회합니다.")
    public ResponseEntity<Page<MarketResponse>> listByDistanceRange(
            @RequestParam(defaultValue = "0") int min,
            @RequestParam(defaultValue = "5000") int max,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "distance") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        if (page < 1) page = 1;
        if (perPage < 1) perPage = 10;
        if (perPage > 100) perPage = 100;

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page - 1, perPage, sort);

        Page<Market> result = marketCrudService.findByDistanceRange(min, max, pageable);
        return ResponseEntity.ok(result.map(MarketResponse::from));
    }

    /** 좌표 업데이트 */
    @PatchMapping("/db/{id}/location")
    @Operation(summary = "시장 좌표 저장/수정")
    public ResponseEntity<Void> updateLocation(
            @PathVariable Long id,
            @RequestBody @Valid MarketLocationRequest req
    ) {
        marketCrudService.updateLocation(id, req.getLatitude(), req.getLongitude());
        return ResponseEntity.noContent().build();
    }

    /** 거리 업데이트 */
    @PatchMapping("/db/{id}/distance")
    @Operation(summary = "시장 거리 저장/수정")
    public ResponseEntity<Void> updateDistance(
            @PathVariable Long id,
            @RequestBody @Valid MarketDistanceRequest req
    ) {
        marketCrudService.updateDistance(id, req.getDistance());
        return ResponseEntity.noContent().build();
    }}