package com.lionkit.mogumarket.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupPurchaseStatus {

    WAITING("WAITING", "공동구매 대기"),
    IN_PROGRESS("IN_PROGRESS", "공동구매 진행 중"),
    SUCCESS("SUCCESS", "공동구매 성사 성공"),
    FAILED("FAILED", "공동구매 성사 실패");

    private final String key;
    private final String title;
}