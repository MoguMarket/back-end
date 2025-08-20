package com.lionkit.mogumarket.payment.portone.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PortonePgVendor {
    KG_INICIS("KG_INICIS", "KG 이니시스"),
    KCP("KCP", "KCP"),
    NICE("NICE", "나이스페이"),
    TOSS_PAYMENTS("TOSS_PAYMENTS", "토스페이먼츠"),
    SMARTRO("SMARTRO", "스마트로"),
    ETC("ETC", "기타");

    private final String key;
    private final String title;
}