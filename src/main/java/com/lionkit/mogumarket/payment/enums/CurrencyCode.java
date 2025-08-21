package com.lionkit.mogumarket.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurrencyCode {
    KRW("KRW", "대한민국 원"),
    USD("USD", "미국 달러"),
    JPY("JPY", "일본 엔"),
    EUR("EUR", "유로"),
    CNY("CNY", "중국 위안"),
    GBP("GBP", "영국 파운드"),
    HKD("HKD", "홍콩 달러"),
    SGD("SGD", "싱가포르 달러"),
    AUD("AUD", "호주 달러"),
    CAD("CAD", "캐나다 달러"),
    TWD("TWD", "대만 달러"),
    THB("THB", "태국 바트"),
    VND("VND", "베트남 동"),
    PHP("PHP", "필리핀 페소"),
    IDR("IDR", "인도네시아 루피아"),
    INR("INR", "인도 루피"),
    MYR("MYR", "말레이시아 링깃"),
    NZD("NZD", "뉴질랜드 달러"),
    CHF("CHF", "스위스 프랑");

    private final String key;    // ISO 4217 코드
    private final String title;  // 표시용 한글 명칭


}