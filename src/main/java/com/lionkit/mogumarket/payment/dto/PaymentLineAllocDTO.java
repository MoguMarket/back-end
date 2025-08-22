package com.lionkit.mogumarket.payment.dto;

import lombok.*;

/**
 * 단독으로 request/response 받는데 쓰지 않고
 * 다른 request/response ( ex : PaymentInitiateRequest  )에 포함되어 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentLineAllocDTO {

    /**
     * 전체 청구 중 어느 상품에 대한 order 에 대한 청구인지
     */
    private Long orderLineId;

    private Long cashAmount;   // 원 단위
    private Long pointAmount;  // 포인트(=원) 1:1 가정
}
