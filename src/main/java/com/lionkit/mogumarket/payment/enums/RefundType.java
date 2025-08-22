package com.lionkit.mogumarket.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundType {
    CASH("CASH", "현금(카드/이체 등)"),
    POINT("POINT", "포인트")
    ;

    private final String key;
    private final String title;

}
