package com.lionkit.mogumarket.point.controller;

import com.lionkit.mogumarket.point.dto.request.PointEarnRequestDto;
import com.lionkit.mogumarket.point.dto.request.PointRedeemRequestDto;
import com.lionkit.mogumarket.point.dto.response.PointSnapshotResponseDto;
import com.lionkit.mogumarket.point.service.PointService;
import com.lionkit.mogumarket.security.oauth2.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/points", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Points", description = "포인트 조회/적립/차감 API")
public class PointController {

    private final PointService pointService;

    @GetMapping("/me")
    @Operation(
            summary = "내 포인트 스냅샷 조회",
            description = "로그인한 사용자의 포인트 잔액/홀드/사용 가능액 스냅샷을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PointSnapshotResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public PointSnapshotResponseDto getLoginedUserPointSnapshot(
            @AuthenticationPrincipal
            @Parameter(description = "인증 사용자") PrincipalDetails loginedUser
    ) {
        Long userId = loginedUser.getUser().getId();
        return PointSnapshotResponseDto.fromEntity(pointService.getSnapshot(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    @Operation(
            summary = "특정 유저 포인트 스냅샷 조회(관리자)",
            description = "관리자 권한으로 특정 사용자의 포인트 스냅샷을 조회합니다."
    )
    @Parameters({
            @Parameter(name = "userId", description = "조회할 사용자 ID", required = true, example = "42")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PointSnapshotResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 포인트 지갑 없음")
    })
    public PointSnapshotResponseDto getPointSnapshotById(@PathVariable Long userId) {
        return PointSnapshotResponseDto.fromEntity(pointService.getSnapshot(userId));
    }

    @PostMapping(value = "/earn", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "포인트 적립",
            description = "이벤트/관리자 보정 등 독립적인 사유로 포인트를 적립합니다. 멱등키(idempotencyKey)로 중복 요청을 방지합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "포인트 적립 요청 바디",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PointEarnRequestDto.class),
                            examples = @ExampleObject(name = "예시", value = """
                                    {
                                      "amount": 5000,
                                      "idempotencyKey": "earn-evt-240901-0001"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "적립 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PointSnapshotResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 바디 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public PointSnapshotResponseDto earn(
            @AuthenticationPrincipal
            @Parameter(description = "인증 사용자") PrincipalDetails loginedUser,
            @RequestBody @Valid PointEarnRequestDto request
    ) {
        Long userId = loginedUser.getUser().getId();
        return pointService.earn(userId, request.getAmount(), request.getIdempotencyKey(), null);
    }

    @PostMapping(value = "/redeem", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "포인트 차감(독립)",
            description = "관리자 보정/운영툴 등 **결제와 무관하게** 포인트를 차감합니다. 결제 플로우에서의 차감은 PaymentService가 내부적으로 처리합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "포인트 차감 요청 바디",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PointRedeemRequestDto.class),
                            examples = @ExampleObject(name = "예시", value = """
                                    {
                                      "amount": 3000,
                                      "idempotencyKey": "redeem-ops-240901-0007"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차감 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PointSnapshotResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 바디 유효성 오류 / 잔액 부족"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public PointSnapshotResponseDto burn(
            @AuthenticationPrincipal
            @Parameter(description = "인증 사용자") PrincipalDetails loginedUser,
            @RequestBody @Valid PointRedeemRequestDto request
    ) {
        Long userId = loginedUser.getUser().getId();
        return pointService.burn(userId, request.getAmount(), request.getIdempotencyKey(), null);
    }
}
