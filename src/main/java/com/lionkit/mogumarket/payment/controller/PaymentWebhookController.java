package com.lionkit.mogumarket.payment.controller;


import com.lionkit.mogumarket.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhooks/portone")
    public ResponseEntity<String> portoneWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-PORTONE-WEBHOOK-ID", required = false) String msgId,
            @RequestHeader(value = "X-PORTONE-WEBHOOK-SIGNATURE", required = false) String signature,
            @RequestHeader(value = "X-PORTONE-WEBHOOK-TIMESTAMP", required = false) String timestamp
    ) {
        // 서명 검증 실패 시 서비스 내부에서 예외 발생 → 글로벌 예외핸들러가 4xx/5xx 매핑
        paymentService.handlePortoneWebhook(rawBody, msgId, signature, timestamp);
        return ResponseEntity.ok("OK");
    }


}
