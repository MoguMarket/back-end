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
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                // Security 레이어에서 CORS 활성화 (아래 corsConfigurationSource() 사용)
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        // 프리플라이트는 모두 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

//                        // 공개 엔드포인트 (기존 것 유지 + 수정)
//                        .requestMatchers(
//                                "/api/auth/login",
//                                "/api/user/sign-up",
//                                "/admin/sync-products",       // ← 앞 슬래시 보정
//                                "/api/search",
//                                "/api/search/trending",
//                                "/api/auth/me"
//                        ).permitAll()
//
//                        // FCM: 공개는 vapid-key만, 나머지는 인증 필요
//                        .requestMatchers(HttpMethod.GET, "/api/fcm/web/vapid-key").permitAll()
//                        .requestMatchers("/api/fcm/**").authenticated()
//
//                        // 그 외는 기존 정책대로
//                        .requestMatchers(
//                                "/api/carts/**",
//                                "/api/user/complete-sign-up"
//                        ).authenticated()

                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(c -> c.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                // JWT 필터
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


}