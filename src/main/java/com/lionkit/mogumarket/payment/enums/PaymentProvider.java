package com.lionkit.mogumarket.payment.enums;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentProvider {
    PORTONE("PORTONE", "포트원"),
    OPEN_BANKING("OPEN_BANKING", "오픈뱅킹"),
    ETC("ETC", "기타");

    private final String key;
    private final String title;
}