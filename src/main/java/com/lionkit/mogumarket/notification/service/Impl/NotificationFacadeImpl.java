package com.lionkit.mogumarket.notification.service.Impl;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.lionkit.mogumarket.alarm.service.FCMService;
import com.lionkit.mogumarket.notification.service.NotificationFacade;
import com.lionkit.mogumarket.notification.service.NotificationTemplateResolver;
import com.lionkit.mogumarket.notification.service.TokenQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationFacadeImpl implements NotificationFacade {

    private final FCMService fcmService; // 기존 alarm의 서비스
    private final TokenQueryPort tokenQueryPort;
    private final NotificationTemplateResolver templates;

    @Override
    public void groupBuyReached(Long groupBuyId, List<Long> userIds) {
        var t = templates.groupBuyReached(groupBuyId);
        notifyUsers(userIds, t.title(), t.body(), t.data());
    }

    @Override
    public void groupBuyClosingSoon(Long groupBuyId, List<Long> userIds) {
        var t = templates.groupBuyClosingSoon(groupBuyId);
        notifyUsers(userIds, t.title(), t.body(), t.data());
    }

    @Override
    public void groupBuyClosed(Long groupBuyId, List<Long> userIds) {
        var t = templates.groupBuyClosed(groupBuyId);
        notifyUsers(userIds, t.title(), t.body(), t.data());
    }

    @Override
    public void notifyUsers(List<Long> userIds, String title, String body, Map<String,String> data) {
        var map = tokenQueryPort.tokensOf(userIds);
        map.forEach((userId, tokens) -> {
            if (tokens.isEmpty()) return;
            try {
                // 현재 FCMService에 데이터(payload) 지원 메서드가 없으면 하나 추가하거나,
                // sendToUser(userId, title, body) 반복 호출로 대체
                // 여기서는 간단히 유저별 전송으로 재사용
                fcmService.sendToUser(userId, title, body);
            } catch (FirebaseMessagingException e) {
                log.warn("FCM send failed. userId={}, reason={}", userId, e.getMessage());
            }
        });
    }
}