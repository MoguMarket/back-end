package com.lionkit.mogumarket.alarm.dto;

import lombok.Data;

@Data
public class FCMRegisterRequest {
    private Long userId;
    private String fcmToken;
}