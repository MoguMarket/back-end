package com.lionkit.mogumarket.payment.controller;


import com.lionkit.mogumarket.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhooks/portone")
    @Operation(
            summary = "포트원 웹훅 수신",
            description = "PortOne에서 오는 결제/취소 이벤트를 수신합니다. " +
                    "서명 검증 후, 서버 재조회로 상태를 동기화합니다.")
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
