package com.test.oauth.security.jwt.controller;

import com.test.oauth.global.base.response.ResponseBody;
import com.test.oauth.global.base.response.ResponseUtil;
import com.test.oauth.security.jwt.dto.request.UsernameLoginRequestDto;
import com.test.oauth.security.jwt.dto.response.TokenResponseDto;
import com.test.oauth.security.jwt.service.AuthService;
import com.test.oauth.security.oauth2.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "Access Token / Refresh Token 기반 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "자체 로그인",
            description = "username, password를 이용해 로그인을 수행합니다. 최종 회원가입 미완료된 사용자에 대해선 임시 accessToken만, 최종 회원가입까지 완료한 사용자에 대해서는 정식 access / refresh token 을 발급합니다. "
    )
    @PostMapping("/login")
    public ResponseEntity<ResponseBody<TokenResponseDto>> usernameLogin(
            @RequestBody UsernameLoginRequestDto request
    ) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(authService.usernameLogin(request)));
    }

    @Operation(
            summary = "임시 access token을 이용한 최종 oauth 로그인",
            description = "최종 회원가입까지 완료한 사용자의 OAuth2 로그인 과정에서 발급받은 임시 access token을 통해 정식 access / refresh token 을 발급합니다.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @PostMapping("/oauth-login")
    public ResponseEntity<ResponseBody<TokenResponseDto>> issueWithTempAccessToken(
            @Parameter(description = "..ㅡ...", hidden = true)
            @RequestHeader("Authorization") String bearerToken,

            @RequestHeader(value = "Device-Id", required = false) String deviceId
    ) {
        TokenResponseDto tokenDto = authService.issueTokensWithTemporaryAccessToken(bearerToken, deviceId);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(tokenDto));
    }

    @Operation(
            summary = "Refresh token을 이용한 토큰 재발급",
            description = "refresh token을 통해 access / refresh token 쌍을 재발급 및 갱신합니다.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @PostMapping("/refresh")
    public ResponseEntity<ResponseBody<TokenResponseDto>> reissueTokens(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String bearerToken
    ) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(authService.reissueTokens(bearerToken)));
    }

    @Operation(
            summary = "단일 로그아웃",
            description = "특정 refresh token을 만료시킵니다.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @DeleteMapping("/logout")
    public ResponseEntity<ResponseBody<Void>> logout(

            @Parameter(hidden = true)
            @RequestHeader("Authorization") String bearerToken
    ) {
        authService.logout(bearerToken);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse());
    }

    @Operation(
            summary = "전체 로그아웃",
            description = "특정 유저에 대해 등록된 모든 refresh token을 만료시킵니다.",
            security = @SecurityRequirement(name = "BearerAuth")

    )
    @DeleteMapping("/logout/all")
    public ResponseEntity<ResponseBody<Void>> logoutAll(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        authService.logoutAll(principal.getUser().getId());
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse());
    }
}
