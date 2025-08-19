package com.lionkit.mogumarket.store.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StoreSaveRequest {
    private String name;
    private String description;
    private String address;
    private String phone;
    private String thumbnailUrl;

    private Long marketId; // optional
    private Long userId;   // optional (사장님 사용자)
}