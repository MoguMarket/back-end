package com.lionkit.mogumarket.notification.service;

import java.util.List;
import java.util.Map;

public interface TokenQueryPort {
    /** 각 userId별로 토큰 리스트를 반환 */
    Map<Long, List<String>> tokensOf(List<Long> userIds);
}