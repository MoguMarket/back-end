package com.lionkit.mogumarket.category.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {

    AGRICULTURE("AGRICULTURE", "농산물"),
    SEAFOOD("SEAFOOD", "수산물"),
    LIVESTOCK("LIVESTOCK", "축산물"),
    SIDE_PROCESSED("SIDE_PROCESSED", "반찬/가공식품"),
    HEALTH("HEALTH", "건강식품"),
    RICE_SNACK("RICE_SNACK", "떡/전통간식"),
    KITCHEN_DAILY("KITCHEN_DAILY", "주방/생활용품"),
    FASHION("FASHION", "의류/잡화"),
    BEAUTY("BEAUTY", "화장품/뷰티"),
    FLOWER_GARDEN("FLOWER_GARDEN", "화훼/조경"),
    TOOL_HARDWARE("TOOL_HARDWARE", "공구/철물");

    private final String key;
    private final String title;
}
