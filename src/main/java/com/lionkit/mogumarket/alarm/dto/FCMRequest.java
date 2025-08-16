package com.lionkit.mogumarket.alarm.dto;

import lombok.Data;

@Data
public class FCMRequest {
    private String targetToken;
    private String title;
    private String body;
}