// com.lionkit.mogumarket.alarm.service.FCMService
package com.lionkit.mogumarket.alarm.service;

import com.google.firebase.messaging.*;
import com.lionkit.mogumarket.alarm.entity.Alarm;
import com.lionkit.mogumarket.alarm.repository.FcmTokenRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FCMService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    /** (웹 최적화) 단일 토큰 전송 */
    @Transactional
    public String sendMessageToToken(String token, String title, String body) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        WebpushNotification webpushNotification = WebpushNotification.builder()
                .setTitle(title)
                .setBody(body)
                .setIcon("https://your.cdn/icon-192.png") // 아이콘 경로(https 권장)
                .build();

        WebpushConfig webpush = WebpushConfig.builder()
                .putHeader("TTL", "3600")
                .setNotification(webpushNotification)
                .putData("click_action", "/notifications") // SW에서 열 경로
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .setWebpushConfig(webpush)
                .build();

        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            // 등록 해제/만료 토큰 정리
            if (MessagingErrorCode.UNREGISTERED.equals(e.getMessagingErrorCode())
                    || MessagingErrorCode.INVALID_ARGUMENT.equals(e.getMessagingErrorCode())) {
                fcmTokenRepository.findByToken(token).ifPresent(a -> fcmTokenRepository.deleteById(a.getId()));
            }
            throw e;
        }
    }

    /** 내 모든 기기에 멀티캐스트 전송 (실패 토큰 청소 포함) */
    @Transactional
    public SendResult sendToUser(Long userId, String title, String body) throws FirebaseMessagingException {
        List<Alarm> alarms = fcmTokenRepository.findAllByUserId(userId);
        if (alarms.isEmpty()) return new SendResult(0, 0, List.of());

        List<String> tokens = alarms.stream().map(Alarm::getToken).toList();

        WebpushNotification webpushNotification = WebpushNotification.builder()
                .setTitle(title)
                .setBody(body)
                .setIcon("https://your.cdn/icon-192.png")
                .build();

        WebpushConfig webpush = WebpushConfig.builder()
                .putHeader("TTL", "3600")
                .setNotification(webpushNotification)
                .putData("click_action", "/notifications")
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setWebpushConfig(webpush)
                .build();

        BatchResponse resp = FirebaseMessaging.getInstance().sendMulticast(message);

        List<String> toDelete = new ArrayList<>();
        for (int i = 0; i < resp.getResponses().size(); i++) {
            var r = resp.getResponses().get(i);
            if (!r.isSuccessful() && r.getException() != null) {
                MessagingErrorCode code = r.getException().getMessagingErrorCode();
                if (MessagingErrorCode.UNREGISTERED.equals(code) || MessagingErrorCode.INVALID_ARGUMENT.equals(code)) {
                    toDelete.add(tokens.get(i));
                }
            }
        }
        // 실패 토큰 정리
        for (String t : toDelete) {
            fcmTokenRepository.findByToken(t).ifPresent(a -> fcmTokenRepository.deleteById(a.getId()));
        }
        return new SendResult(resp.getSuccessCount(), resp.getFailureCount(), toDelete);
    }

    /** 멱등 저장/갱신: 동일 토큰이면 스킵, 타 유저 소유면 재할당 */
    @Transactional
    public void saveOrUpdateToken(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Optional<Alarm> existing = fcmTokenRepository.findByToken(token);
        if (existing.isPresent()) {
            Alarm a = existing.get();
            if (!a.getUser().getId().equals(userId)) {
                a.setUser(user);
                fcmTokenRepository.save(a);
            }
            return; // 멱등
        }
        Alarm newAlarm = Alarm.builder().user(user).token(token).build();
        fcmTokenRepository.save(newAlarm);
    }

    @Transactional
    public void deleteToken(Long userId, String token) {
        fcmTokenRepository.deleteByUserIdAndToken(userId, token);
    }

    @Transactional
    public void deleteAllTokens(Long userId) {
        fcmTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public List<String> getUserTokens(Long userId) {
        return fcmTokenRepository.findAllByUserId(userId).stream().map(Alarm::getToken).toList();
    }

    /** 멀티캐스트 결과 DTO */
    public record SendResult(int successCount, int failureCount, List<String> cleanedUpTokens) {}
}