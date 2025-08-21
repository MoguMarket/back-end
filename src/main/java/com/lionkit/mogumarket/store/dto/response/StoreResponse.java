package com.lionkit.mogumarket.store.dto.response;

import com.lionkit.mogumarket.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String thumbnailUrl;

    private Long marketId;
    private String marketName;
    private Long userId;
    private String userName; // 필요 시

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .phone(store.getPhone())
                .thumbnailUrl(store.getThumbnailUrl())
                .marketId(store.getMarket() != null ? store.getMarket().getId() : null)
                .marketName(store.getMarket() != null ? store.getMarket().getName() : null)
                .userId(store.getUser() != null ? store.getUser().getId() : null)
                .userName(store.getUser() != null ? store.getUser().getUsername() : null) // User 엔티티에 getName() 있다고 가정
                .build();
    }
}