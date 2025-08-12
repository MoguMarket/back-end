package com.lionkit.mogumarket.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Unit {

    EA("EA", "개", 1.0),     // 기준단위: EA
    G("G", "g", 1.0),        // 기준단위: g
    KG("KG", "kg", 1000.0),  // 1kg = 1000g
    ML("ML", "ml", 1.0),     // 기준단위: ml
    L("L", "L", 1000.0);     // 1L = 1000ml

    private final String key;
    private final String title;
    /** 기준단위로의 환산 계수 (예: KG -> 1000(g)) */
    private final Double toBase;

    public boolean isWeight() { return this == G || this == KG; }
    public boolean isVolume() { return this == ML || this == L; }
    public boolean isCount()  { return this == EA ; }

    /** 표시 단위 수량 → 기준단위 수량으로 변환 */
    public Double toBaseQty(Double qtyInThisUnit) {
        return qtyInThisUnit * toBase;
    }

    /** 기준단위 수량 → 표시 단위 수량으로 변환 */
    public Double fromBaseQty(Double qtyInBaseUnit) {
        return qtyInBaseUnit / toBase;
    }

}
