package com.lionkit.mogumarket.alarm.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.lionkit.mogumarket.alarm.dto.FCMRegisterRequest;
import com.lionkit.mogumarket.alarm.dto.FCMRequest;
import com.lionkit.mogumarket.alarm.service.FCMService;
import com.lionkit.mogumarket.security.oauth2.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fcm")
@Tag(name = "FCM API", description = "Firebase Cloud Messaging 관련 API")
@RequiredArgsConstructor
@Validated
public class FCMController {

    private final FCMService fcmService;

    @Value("${firebase.web.vapid-key:}")
    private String webVapidPublicKey;

    @GetMapping("/web/vapid-key")
    @Operation(
            summary = "웹용 VAPID Public Key 조회",
            description = "웹 클라이언트가 getToken에 사용할 공개키를 제공합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 키 반환",
                            content = @Content(schema = @Schema(example = "{ \"vapidKey\": \"PUBLIC_KEY_VALUE\" }"))),
                    @ApiResponse(responseCode = "404", description = "VAPID Key 미설정")
            }
    )
    public ResponseEntity<?> getWebVapidPublicKey() {
        if (webVapidPublicKey == null || webVapidPublicKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "VAPID public key not configured"));
        }
        return ResponseEntity.ok(Map.of("vapidKey", webVapidPublicKey));
    }

    @PostMapping("/register")
    @Operation(
            summary = "FCM 토큰 등록/갱신",
            description = "클라이언트에서 발급받은 FCM 토큰을 등록하거나 갱신합니다(멱등)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<?> registerToken(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody(
                    description = "등록할 FCM 토큰",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{ \"fcmToken\": \"sample_fcm_token\" }"))
            ) FCMRegisterRequest request
    ) {
        Long userId = principal.getUser().getId();
        fcmService.saveOrUpdateToken(userId, request.getFcmToken());
        return ResponseEntity.ok(Map.of("message", "Token registered"));
    }

    @DeleteMapping("/token")
    @Operation(
            summary = "FCM 토큰 삭제",
            description = "해당 사용자의 특정 기기 토큰을 삭제합니다.",
            parameters = {
                    @Parameter(name = "token", description = "삭제할 FCM 토큰 값", required = true)
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "토큰 없음")
    })
    public ResponseEntity<Void> deleteToken(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam String token
    ) {
        Long userId = principal.getUser().getId();
        fcmService.deleteToken(userId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tokens/me")
    @Operation(
            summary = "내 토큰 목록 조회",
            description = "현재 로그인한 사용자의 등록된 모든 토큰을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "토큰 목록 반환",
            content = @Content(schema = @Schema(example = "{ \"tokens\": [\"token1\", \"token2\"] }")))
    public ResponseEntity<?> myTokens(@AuthenticationPrincipal PrincipalDetails principal) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(Map.of("tokens", fcmService.getUserTokens(userId)));
    }

    @PostMapping("/send/token")
    @Operation(
            summary = "단일 토큰 전송(테스트)",
            description = "지정된 토큰에 알림을 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전송 성공"),
            @ApiResponse(responseCode = "502", description = "FCM 전송 실패")
    })
    public ResponseEntity<?> sendToToken(
            @Valid @RequestBody(
                    description = "푸시 메시지 전송 요청",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{ \"targetToken\": \"token_value\", \"title\": \"알림 제목\", \"body\": \"알림 본문\" }"))
            ) FCMRequest request
    ) {
        try {
            String id = fcmService.sendMessageToToken(request.getTargetToken(), request.getTitle(), request.getBody());
            return ResponseEntity.ok(Map.of("message", "Message sent", "id", id));
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "FCM send failed", "code", String.valueOf(e.getMessagingErrorCode())));
        }
    }

    @PostMapping("/send/me")
    @Operation(
            summary = "내 모든 기기에 전송",
            description = "현재 사용자 계정에 등록된 모든 기기로 멀티캐스트 전송합니다."
    )
    @ApiResponse(responseCode = "200", description = "전송 결과 반환",
            content = @Content(schema = @Schema(example = "{ \"successCount\": 1, \"failureCount\": 0, \"cleanedUpTokens\": [] }")))
    public ResponseEntity<?> sendToMe(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody(
                    description = "알림 제목/본문",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{ \"title\": \"테스트 알림\", \"body\": \"알림 본문\" }"))
            ) Map<String, String> payload
    ) throws FirebaseMessagingException {
        Long userId = principal.getUser().getId();
        String title = payload.getOrDefault("title", "");
        String body  = payload.getOrDefault("body", "");
        var result = fcmService.sendToUser(userId, title, body);
        return ResponseEntity.ok(Map.of(
                "successCount", result.successCount(),
                "failureCount", result.failureCount(),
                "cleanedUpTokens", result.cleanedUpTokens()
        ));
    }
}