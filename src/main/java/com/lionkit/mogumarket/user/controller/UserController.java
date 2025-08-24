package com.lionkit.mogumarket.user.controller;

import com.lionkit.mogumarket.global.base.response.ResponseBody;
import com.lionkit.mogumarket.global.base.response.ResponseUtil;
import com.lionkit.mogumarket.security.oauth2.principal.PrincipalDetails;
import com.lionkit.mogumarket.user.dto.request.SignUpRequestDto;
import com.lionkit.mogumarket.user.dto.response.UserResponseDto;
import com.lionkit.mogumarket.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "유저 및 회원가입 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "자체 회원가입", description = "자체 로그인 기반의 회원가입을 수행합니다.")
    @PostMapping("/sign-up")
    public ResponseEntity<ResponseBody<UserResponseDto>> signup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 요청 정보", required = true,
                    content = @Content(schema = @Schema(hidden = true))
            )
            @RequestBody SignUpRequestDto requestDto
    ) {
        UserResponseDto responseDto = userService.signup(requestDto);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(responseDto));
    }

    @GetMapping("/me")
    public UserResponseDto me(@AuthenticationPrincipal PrincipalDetails principal) {
        return userService.getUserInfoById(principal.getUser().getId());
    }


}
