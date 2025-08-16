package com.lionkit.mogumarket.alarm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.lionkit.mogumarket.alarm.entity.Alarm;
import com.lionkit.mogumarket.alarm.repository.FcmTokenRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FCMService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    public FCMService( FcmTokenRepository fcmTokenRepository, UserRepository userRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.userRepository = userRepository;
    }

    // FCM 메시지 전송
    public String sendMessage(String targetToken, String title, String body) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(notification)
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .build();

        return FirebaseMessaging.getInstance().send(message);
    }

    // FCM 토큰 저장 또는 업데이트
    public void saveOrUpdateToken(Long userId, String token) {
        // 1. userId로 User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 2. 해당 user의 알림 토큰 존재 여부 확인
        Optional<Alarm> optional = fcmTokenRepository.findByUser(user);

        if (optional.isPresent()) {
            Alarm alarm = optional.get();
            alarm.setToken(token);
            fcmTokenRepository.save(alarm);
        } else {
            Alarm newAlarm = Alarm.builder()
                    .user(user)
                    .token(token)
                    .build();
            fcmTokenRepository.save(newAlarm);
        }
    }
}