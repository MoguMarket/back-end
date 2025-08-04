package com.test.oauth.security.jwt.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TokenStatus {
    AUTHENTICATED,
    EXPIRED,
    INVALID
}
