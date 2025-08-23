package com.lionkit.mogumarket.alarm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class FCMRegisterRequest {

    @NotBlank(message = "fcmToken is required")
    @JsonProperty("fcmToken") // JSON 키명과 1:1
    private String fcmToken;

    // 인증 주체가 없을 때 대비(옵션): 바디에 userid도 허용하고 싶으면 추가
    @JsonProperty("userid")
    private Long userId;
}