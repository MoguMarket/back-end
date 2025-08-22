package com.lionkit.mogumarket.security.jwt.principal;


/**
 * 자체로그인만을 위한 principal 구현체.
 * oauth2 를 도입하므로써 이는 PrincipalDetails 로 대체하였습니다.
 */
//@Getter
//@Builder
//public class CustomUserDetails implements UserDetails {
//    private final User user;
//    private final Collection<? extends GrantedAuthority> authorities;
//
//    public CustomUserDetails(User user) {
//        this.user = user;
//        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole().getKey()));
//    }
//
//    @Override
//    public String getUsername() {
//        return user.getUsername();
//    }
//
//    @Override
//    public String getPassword() {
//        return user.getPassword();
//    }
//
//    @Override public boolean isAccountNonExpired() { return true; }
//    @Override public boolean isAccountNonLocked() { return true; }
//    @Override public boolean isCredentialsNonExpired() { return true; }
//    @Override public boolean isEnabled() { return true; }
//
//}
//
//
