package com.lionkit.mogumarket.payment.controller;


import com.lionkit.mogumarket.payment.dto.request.RefundLinesRequest;
import com.lionkit.mogumarket.payment.dto.request.CreatePaymentRequest;
import com.lionkit.mogumarket.payment.dto.response.CreatePaymentResponse;
import com.lionkit.mogumarket.payment.dto.response.PaymentSnapshotResponse;
import com.lionkit.mogumarket.payment.dto.response.LineRefundResponse;
import com.lionkit.mogumarket.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "결제 API", description = "결제 생성/조회/환불/웹훅")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    // ---------- 1) 결제 준비 (merchantUid 발급) ----------
    @PostMapping
    @Operation(summary = "결제 준비", description = "주문에 대한 결제 엔티티를 생성하고 merchantUid를 발급합니다.")
    public ResponseEntity<CreatePaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest req) {
        var res = paymentService.createPaymentReady(
                req.ordersId(),
                req.paidCashAmount(),
                req.paidPointAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreatePaymentResponse.from(res));
    }

    // ---------- 2) 결제 스냅샷 조회 ----------
    @GetMapping("/{paymentId}")
    @Operation(summary = "결제 스냅샷 조회", description = "Payment 현재 스냅샷(상태/금액 등)을 조회합니다.")
    public ResponseEntity<PaymentSnapshotResponse> getSnapshot(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getSnapshot(paymentId));
    }

    // ---------- 3) (부분)환불 - 라인 단위 ----------
    @PostMapping("/{paymentId}/cancel-lines")
    @Operation(
            summary = "부분환불(라인 단위)",
            description = "orderLine/paymentLine 단위로 환불을 수행합니다. " +
                    "PG에는 합계 금액으로 한 번 취소 요청이 나가며, 내부적으로 라인별 환불액을 갱신합니다.")
    public ResponseEntity<LineRefundResponse> cancelByLines(
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundLinesRequest req
    ) {
        LineRefundResponse res = paymentService.cancelByLines(
                paymentId,
                req.lines(),
                req.reason(),
                req.refundType()
        );
        return ResponseEntity.ok(res);
    }







}
