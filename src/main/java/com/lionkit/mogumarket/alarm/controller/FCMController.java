package com.lionkit.mogumarket.alarm.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.lionkit.mogumarket.alarm.dto.FCMRequest;
import com.lionkit.mogumarket.alarm.service.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@Tag(name = "FCM API", description = "Firebase Cloud Messaging 관련 API")
public class FCMController {

    private final FCMService fcmService;

    public FCMController(FCMService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/send")
    @Operation(
            summary = "FCM 메시지 전송",
            description = "Firebase Cloud Messaging을 통해 푸시 알림을 전송합니다."
    )
    public ResponseEntity<?> sendPush(@RequestBody FCMRequest request) {
        try {
            String response = fcmService.sendMessage(
                    request.getTargetToken(),
                    request.getTitle(),
                    request.getBody()
            );
            return ResponseEntity.ok("Message sent: " + response);
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}