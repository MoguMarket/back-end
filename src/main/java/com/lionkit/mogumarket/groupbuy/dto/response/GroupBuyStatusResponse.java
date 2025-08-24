package com.lionkit.mogumarket.groupbuy.dto.response;

import com.lionkit.mogumarket.groupbuy.enums.GroupBuyStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupBuyStatusResponse {
    private Long groupBuyId;
    private GroupBuyStatus status;
    private int stageCount;


    private double totalQuantity;         // 누적 참여 수량
    private double targetQuantity;
    // 목표 수량
    private double currentDiscount;       // 현재 할인율(%)
    private double remainingToNextStage;  // 다음 단계까지 남은 수량 (0이면 최상단계 or 단계없음)

    private double originalUnitPrice;      // 상품 기준단위 원가
    private long appliedUnitPrice;         // 현재 단계 할인 반영 단가(원, 반올림)

    private LocalDateTime endAt; // 마감 시간


}