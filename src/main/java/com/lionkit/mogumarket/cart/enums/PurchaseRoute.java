package com.lionkit.mogumarket.cart.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseRoute {
    NORMAL("NORMAL", "일반 구매"),
    GROUP_BUY("GROUP_BUY","공동 구매");
    private final String key, title;

}




