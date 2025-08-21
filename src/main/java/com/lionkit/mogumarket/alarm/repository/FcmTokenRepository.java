package com.lionkit.mogumarket.alarm.repository;

import com.lionkit.mogumarket.alarm.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<Alarm, Long> {

        Optional<Alarm> findByToken(String token);

        List<Alarm> findAllByUserId(Long userId);

        boolean existsByUserIdAndToken(Long userId, String token);

        void deleteByUserId(Long userId);

        void deleteByUserIdAndToken(Long userId, String token);
}