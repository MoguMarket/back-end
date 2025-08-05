package com.lionkit.mogumarket.security.jwt.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    /**
     * NOT_REGISTERED 이면 -> 회원가입 페이지로 redirect
     * NOT_REGISTERED가 아니면 -> 서비스 페이지로 redirect
     */
    NOT_REGISTERED("ROLE_NOT_REGISTERED", "회원가입 이전 사용자"),
    USER("ROLE_USER", "회원가입 완료된 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
}
