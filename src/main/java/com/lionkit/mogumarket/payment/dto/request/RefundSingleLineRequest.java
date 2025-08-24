package com.lionkit.mogumarket.payment.dto.request;

// DTO: 라인별 환불 요청
public record RefundSingleLineRequest(Long orderLineId, long refundAmount) {}
