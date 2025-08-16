package com.lionkit.mogumarket.security.jwt.dto.request;

import lombok.*;


/**
 * 자체 로그인을 위한 dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsernameLoginRequestDto {
    private String username;
    private String password;
    private String deviceId;
}
