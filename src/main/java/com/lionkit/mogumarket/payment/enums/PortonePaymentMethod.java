package com.lionkit.mogumarket.payment.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PortonePaymentMethod {
    CARD("CARD", "카드"),
    TRANSFER("TRANSFER", "일반계좌"),
    VIRTUAL_ACCOUNT("VIRTUAL_ACCOUNT", "가상계좌"),
    GIFT_CERTIFICATE("GIFT_CERTIFICATE", "상품권"),
    MOBILE("MOBILE", "휴대폰 결제"),
    EASY_PAY("EASY_PAY", "간편결제"),
    ETC("ETC", "기타");

    private final String key;
    private final String title;
}