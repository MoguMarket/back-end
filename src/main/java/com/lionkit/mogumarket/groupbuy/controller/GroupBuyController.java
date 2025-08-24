package com.lionkit.mogumarket.groupbuy.controller;
import com.lionkit.mogumarket.groupbuy.dto.response.GroupBuyStatusResponse;
import com.lionkit.mogumarket.groupbuy.service.GroupBuyService;

import com.lionkit.mogumarket.groupbuy.dto.request.CreateGroupBuyRequest;
import com.lionkit.mogumarket.groupbuy.dto.request.GroupBuyParticipateRequest;
import com.lionkit.mogumarket.groupbuy.dto.response.GroupBuySnapshotResponse;
import com.lionkit.mogumarket.groupbuy.dto.response.MyGroupBuyParticipationResponse;
import com.lionkit.mogumarket.security.oauth2.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groupbuy")
@RequiredArgsConstructor
@Tag(name = "GroupBuy", description = "공동구매 관련 API")
public class GroupBuyController {

    private final GroupBuyService groupBuyService;

    @Operation(summary = "공동구매 생성", description = "상품에 대해 공동구매를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류", content = @Content)
    })
    @PostMapping("/open")
    public ResponseEntity<GroupBuySnapshotResponse> createGroupBuy(@Valid @RequestBody
                                                   CreateGroupBuyRequest request) {

        return ResponseEntity.ok( groupBuyService.createGroupBuy(
                request.productId(),
                request.targetQty(),
                request.maxDiscountPercent(),
                request.stage() ));
    }

    @Operation(summary = "공동구매 참여", description = "특정 공동구매에 유저가 수량을 참여합니다.")
    @PostMapping("/participate")
    public ResponseEntity<GroupBuySnapshotResponse> participate(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody  GroupBuyParticipateRequest request) {

        return ResponseEntity.ok( groupBuyService.participate(request.groupBuyId(), principal.getUser().getId(), request.qty()));
    }

    @GetMapping("/me")
    @Operation(summary = "내 공구 참여 목록", description = "로그인 사용자가 참여중인 모든 공구/단계/참여정보를 반환합니다.")
    public ResponseEntity<MyGroupBuyParticipationResponse> listMyParticipations(
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(groupBuyService.listMyParticipations(userId));
    }


    @Operation(summary = "공동구매 마감", description = "공동구매를 마감 처리합니다.")
    @PostMapping("/{groupBuyId}/close")
    public ResponseEntity<GroupBuySnapshotResponse> closeGroupBuy(
            @Parameter(description = "공동구매 ID", required = true) @PathVariable Long groupBuyId
    ) {

        return ResponseEntity.ok( groupBuyService.closeGroupBuy(groupBuyId));
    }

    @Operation(summary = "공동구매 상태 조회", description = "현재 총 수량/다음 단계까지 남은 수량/적용 단가 등을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GroupBuyStatusResponse.class)))
    })
    @GetMapping("/{groupBuyId}/status")
    public ResponseEntity<GroupBuySnapshotResponse > getStatus(
            @Parameter(description = "공동구매 ID", required = true) @PathVariable Long groupBuyId
    ) {
        return ResponseEntity.ok(groupBuyService.getGroupBuyStatus(groupBuyId));
    }
}