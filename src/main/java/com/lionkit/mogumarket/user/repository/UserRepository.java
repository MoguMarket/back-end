package com.lionkit.mogumarket.user.repository;


import com.lionkit.mogumarket.security.oauth2.enums.ProviderType;
import com.lionkit.mogumarket.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일을 기준으로 사용자 찾기
    Optional<User> findByUsername(String username);

    //OAuth 정보( provider 벤더명과 해당 provider 상의 식별자값 ) 기준으로 사용자 찾기
    @Query("select u from User u where u.providerType = :providerType and u.providerId = :providerId " )
    Optional<User> findByOAuthInfo(@Param("providerType") ProviderType providerType,
                                   @Param("providerId") String providerId);



}