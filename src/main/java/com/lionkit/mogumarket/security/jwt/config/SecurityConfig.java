package com.lionkit.mogumarket.security.jwt.config;

import com.lionkit.mogumarket.security.jwt.filter.JwtAuthenticationFilter;
import com.lionkit.mogumarket.security.jwt.handler.CustomAccessDeniedHandler;
import com.lionkit.mogumarket.security.jwt.handler.CustomAuthenticationEntryPoint;
import com.lionkit.mogumarket.security.jwt.service.CustomUserDetailsService;
import com.lionkit.mogumarket.security.jwt.util.JwtTokenProvider;
import com.lionkit.mogumarket.security.oauth2.handler.OAuth2FailureHandler;
import com.lionkit.mogumarket.security.oauth2.handler.OAuth2SuccessHandler;
import com.lionkit.mogumarket.security.oauth2.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;




    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/user/sign-up",// 로그인, 회원가입
                                "admin/sync-products",// 관리자용 API: 수동으로 상품 데이터를 Elasticsearch에 동기화
                                "/api/search", // 검색
                                "/api/search/trending",// 인기 검색어
                                "/api/fcm/",// FCM 관련 API
                                "/api/fcm/send")
                        .permitAll()
                        .anyRequest().permitAll()
                )

                // oauth2 설정
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(c -> c.userService(customOAuth2UserService)) // 사용자 정보를 어디서 load 할지 설정
                        .successHandler(oAuth2SuccessHandler) // 로그인 성공 시 핸들러
                        .failureHandler(oAuth2FailureHandler) // 로그인 실패 시 핸들러
                )

                // jwt 설정
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService), UsernamePasswordAuthenticationFilter.class);  // JWT 인증 필터 추가;


        return http.build();
    }


}