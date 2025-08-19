package com.test.oauth.security.jwt.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;



/**
 * 자체 로그인을 위한 dto
 */
@Getter
@AllArgsConstructor
@Builder
public class UsernameLoginResponseDto {
    private String access;
    private String refresh;
}