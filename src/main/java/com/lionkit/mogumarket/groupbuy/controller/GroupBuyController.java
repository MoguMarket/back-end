package com.lionkit.mogumarket.groupbuy.controller;

import com.lionkit.mogumarket.groupbuy.dto.response.GroupBuyStatusResponse;
import com.lionkit.mogumarket.groupbuy.service.GroupBuyQueryService;
import com.lionkit.mogumarket.groupbuy.service.GroupBuyService;
import com.lionkit.mogumarket.product.dto.response.ProductGroupBuyOverviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/groupbuy")
@RequiredArgsConstructor
@Tag(name = "GroupBuy", description = "공동구매 관련 API")
public class GroupBuyController {

    private final GroupBuyService groupBuyService;
    private final GroupBuyQueryService groupBuyQueryService;

    @Operation(summary = "공동구매 생성", description = "상품에 대해 공동구매를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류", content = @Content)
    })
    @PostMapping("/{productId}")
    public ResponseEntity<Long> createGroupBuy(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
            @Parameter(description = "목표 수량", required = true) @RequestParam double targetQty,
            @Parameter(description = "최대 할인률(%)", required = false, example = "25")
            @RequestParam(required = false, defaultValue = "0") Double maxDiscountPercent,

            @Parameter(description = "단계 수(기본 3)", required = false, example = "3")
            @RequestParam(required = false, defaultValue = "3") Integer stage,
            @Parameter(description = "공동구매 시작 시간", required = false, example = "2023-10-01T00:00:00")
            @RequestParam(required = false) String startAt,
            @Parameter(description = "공동구매 마감 시간", required = false, example = "2023-10-31T23:59:59")
            @RequestParam(required = false) String endAt

    ) {
        LocalDateTime start = (startAt != null) ? LocalDateTime.parse(startAt) : LocalDateTime.now();
        LocalDateTime end = (endAt != null) ? LocalDateTime.parse(endAt) : LocalDateTime.now().plusDays(7);

        return ResponseEntity.ok(groupBuyService.createGroupBuy(productId, targetQty, maxDiscountPercent, stage,start, end));
    }

    @Operation(summary = "공동구매 참여", description = "특정 공동구매에 유저가 수량을 참여합니다.")
    @PostMapping("/{groupBuyId}/participate")
    public ResponseEntity<Void> participate(
            @Parameter(description = "공동구매 ID", required = true) @PathVariable Long groupBuyId,
            @Parameter(description = "유저 ID", required = true) @RequestParam Long userId,
            @Parameter(description = "참여 수량", required = true) @RequestParam double qty
    ) {
        groupBuyService.participate(groupBuyId, userId, qty);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "공동구매 마감", description = "공동구매를 마감 처리합니다.")
    @PostMapping("/{groupBuyId}/close")
    public ResponseEntity<Void> closeGroupBuy(
            @Parameter(description = "공동구매 ID", required = true) @PathVariable Long groupBuyId
    ) {
        groupBuyService.closeGroupBuy(groupBuyId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "공동구매 상태 조회", description = "현재 총 수량/다음 단계까지 남은 수량/적용 단가 등을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GroupBuyStatusResponse.class)))
    })
    @GetMapping("/{groupBuyId}/status")
    public ResponseEntity<GroupBuyStatusResponse> getStatus(
            @Parameter(description = "공동구매 ID", required = true) @PathVariable Long groupBuyId
    ) {
        return ResponseEntity.ok(groupBuyService.getGroupBuyStatus(groupBuyId));
    }

    @GetMapping("/closing-soon")
    @Operation(
            summary = "마감 임박순 공구 목록",
            description = "OPEN 상태이며 마감 시간이 임박한 순서로 정렬해 반환합니다."
    )
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호(0-base)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "10")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductGroupBuyOverviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Page<ProductGroupBuyOverviewResponse>> listClosingSoon(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(groupBuyQueryService.listClosingSoon(page, size));
    }
}