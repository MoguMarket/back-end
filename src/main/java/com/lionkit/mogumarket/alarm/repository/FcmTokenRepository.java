package com.lionkit.mogumarket.alarm.repository;

import com.lionkit.mogumarket.alarm.entity.Alarm;
import com.lionkit.mogumarket.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<Alarm, Long> {

        Optional<Alarm> findByUser(User user);

        boolean existsByUserId(Long userId);

        void deleteByUserId(Long userId); // optional


}

