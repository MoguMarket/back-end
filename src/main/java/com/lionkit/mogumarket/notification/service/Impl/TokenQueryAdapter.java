// notification/service/impl/TokenQueryAdapter.java
package com.lionkit.mogumarket.notification.service.Impl;

import com.lionkit.mogumarket.alarm.entity.Alarm;
import com.lionkit.mogumarket.alarm.repository.FcmTokenRepository;
import com.lionkit.mogumarket.notification.service.TokenQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TokenQueryAdapter implements TokenQueryPort {

    private final FcmTokenRepository fcmTokenRepository;

    @Override
    public Map<Long, List<String>> tokensOf(List<Long> userIds) {
        // 효율을 위해 리포지토리에 IN 쿼리 메서드가 있으면 더 좋음(예: findAllByUserIdIn)
        // 지금은 간단히 개별 조회로(규모 커지면 최적화)
        Map<Long, List<String>> result = new HashMap<>();
        for (Long uid : userIds) {
            List<String> tokens = fcmTokenRepository.findAllByUserId(uid)
                    .stream().map(Alarm::getToken).toList();
            if (!tokens.isEmpty()) result.put(uid, tokens);
        }
        return result;
    }
}