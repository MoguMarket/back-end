package com.lionkit.mogumarket.payment.portone.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "portone")
public class PortoneProps {

    @Value("${portone.api-secret}")
    private String apiSecret;// 필수: 서버 SDK 비밀키

    @Value("${portone.webhook-secret}")
    private String webhookSecret; // 필수 : 웹훅 서명 검증 시크릿

    @Value("${portone.api-base:https://api.portone.io}")
    private String apiBase; //  선택: API 베이스

    @Value("${portone.store-id}")
    private String storeId; // 선택: 스토어 ID





}
