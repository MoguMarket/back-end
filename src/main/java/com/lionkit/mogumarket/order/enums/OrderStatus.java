package com.lionkit.mogumarket.order.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    /**
     * 공구 참여 완료 = 예약 = 결제 확정 (환불 불가 ->가격/단계 스냅샷 고정)
     **/
    CONFIRMED("CONFIRMED", "예약 확정"),


    /** 공구 종료 시 실제 결제(청구) 완료 */
    PAID("PAID", "결제 완료"),

    /**
     * 규정상 환불 불가하나, 모종의 이유로 취소가 불가피하여 취소된 상태
     */
    CANCELLED_BY_USER("CANCELLED", "취소"),

    /** 예외적 환불 완료 */
    REFUNDED("REFUNDED", "환불 완료"),

    /**
     * 결제 실패, 오류로 인한 실패 상태
     */
    FAILED("FAILED", "결제 실패"),

    /** (선택) 운영자 강제 취소 */
    ADMIN_CANCELLED("ADMIN_CANCELLED", "관리자 취소")

    ;

    private final String key;
    private final String title;
}

