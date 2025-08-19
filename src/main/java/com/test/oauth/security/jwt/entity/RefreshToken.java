package com.test.oauth.security.jwt.entity;

import com.test.oauth.global.base.domain.BaseEntity;
import com.test.oauth.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 다중 기기 로그인 허용을 위해 로그인한 디바이스 정보 저장
     */
    private String deviceId;

    // 사용자 구분을 위해 user_id 저장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 특정 유저의 특정 기기에 대해 발급된 refresh token 을 재발급 시 사용
     * @param token
     * @param expiresAt
     */
    public void update(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }


}
