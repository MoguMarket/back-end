package com.lionkit.mogumarket.security.oauth2.principal;

import com.lionkit.mogumarket.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * SecurityContext 저장용 자체로그인 + oauth2 객체
 * jwt 만 사용한 자체로그인 시 UserDetails, Oauth2 만 사용한 로그인 시 OAuth2User 각각 만 implements 하고 이를 seucritycontext 에 저장해 사용하면 되는데
 * 자체로그인과 OAuth2 를 둘 다 구현한 경우 @AuthenticationPrincipal 을 통해 쉽게 불러오기 위해선 둘 다 implements 하는게 좋다
 */
@Getter
@Builder
public class PrincipalDetails implements OAuth2User, UserDetails {
    private  User user;
    private  Map<String, Object> attributes;

    //일반 로그인 생성자
    public PrincipalDetails(User user) {
        this.user = user;
    }

    //OAuth 로그인 생성자
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }


    @Override
    public String getName() {
        return user.getId() != null ? user.getId().toString() : "anonymous_name";
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().getKey()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername() != null ? user.getUsername()  : "anonymous_username";
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }



}
