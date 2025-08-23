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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestBody;

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

    @GetMapping(value = "/web/vapid-key", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getWebVapidPublicKey() {
        if (webVapidPublicKey == null || webVapidPublicKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "VAPID public key not configured"));
        }
        return ResponseEntity.ok(Map.of("vapidKey", webVapidPublicKey));
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "FCM 토큰 등록/갱신", description = "클라이언트에서 발급받은 FCM 토큰을 등록하거나 갱신합니다(멱등).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"fcmToken\": \"sample_fcm_token\" }")
            )
    )
    public ResponseEntity<?> registerToken(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody FCMRegisterRequest request //
    ) {
        Long userId = (principal != null && principal.getUser() != null)
                ? principal.getUser().getId()
                : request.getUserId(); // (옵션) 바디에 userid 허용 시

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthenticated or userId missing"));
        }

        fcmService.saveOrUpdateToken(userId, request.getFcmToken());
        return ResponseEntity.ok(Map.of("message", "Token registered"));
    }

    @DeleteMapping("/token")
    public ResponseEntity<Void> deleteToken(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam String token
    ) {
        Long userId = principal.getUser().getId();
        fcmService.deleteToken(userId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/tokens/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> myTokens(@AuthenticationPrincipal PrincipalDetails principal) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(Map.of("tokens", fcmService.getUserTokens(userId)));
    }

    @PostMapping(value = "/send/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "단일 토큰 전송(테스트)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전송 성공"),
            @ApiResponse(responseCode = "502", description = "FCM 전송 실패")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"targetToken\": \"token_value\", \"title\": \"알림 제목\", \"body\": \"알림 본문\" }")
            )
    )
    public ResponseEntity<?> sendToToken(
            @Valid @RequestBody FCMRequest request
    ) {
        try {
            String id = fcmService.sendMessageToToken(request.getTargetToken(), request.getTitle(), request.getBody());
            return ResponseEntity.ok(Map.of("message", "Message sent", "id", id));
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "FCM send failed", "code", String.valueOf(e.getMessagingErrorCode())));
        }
    }

    @PostMapping(value = "/send/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "내 모든 기기에 전송")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"title\": \"테스트 알림\", \"body\": \"알림 본문\" }")
            )
    )
    public ResponseEntity<?> sendToMe(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody Map<String, String> payload // ✅ Spring MVC @RequestBody
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