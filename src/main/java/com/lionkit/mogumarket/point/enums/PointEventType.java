package com.lionkit.mogumarket.point.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointEventType {
    EARN("EARN","포인트 적립"),
    REDEEM("REDEEM","포인트 사용"),
    LOCK("LOCK","포인트 락 적용"),
    UN_LOCK("UN_LOCK","포인트 락 해제"),
    REFUND("REFUND","포인트 복구"),
    EXPIRE("EXPIRE","포인트 만료"), // 현재 포인트 만료 기한은 따로 설정하지 않은 상황입니다.
    ADJUST("ADJUST","포인트 수정");

    private final String key, title;

}
