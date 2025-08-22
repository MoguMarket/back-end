// MarketResponse.java
package com.lionkit.mogumarket.market.dto.response;

import com.lionkit.mogumarket.market.entity.Market;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketResponse {
    private Long id;
    private String marketCode;
    private String name;
    private String landAddress;
    private String roadAddress;
    private String sido;
    private String sigungu;
    private Double latitude;
    private Double longitude;
    private Integer distance; // 저장 값 그대로

    public static MarketResponse from(Market m) {
        return MarketResponse.builder()
                .id(m.getId())
                .marketCode(m.getMarketCode())
                .name(m.getName())
                .landAddress(m.getLandAddress())
                .roadAddress(m.getRoadAddress())
                .sido(m.getSido())
                .sigungu(m.getSigungu())
                .latitude(m.getLatitude())
                .longitude(m.getLongitude())
                .distance(m.getDistance())
                .build();
    }
}