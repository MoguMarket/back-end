package com.test.oauth.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("OAuth2 + 자체 로그인 인증 API 문서")
                .description("""
                        OAuth2(Kakao, Google, Naver) 로그인과 자체 로그인 기능을 통합한 인증 시스템입니다.
                        
                        - Access Token / Refresh Token 기반 인증
                        - 기본 회원가입과 최종 회원가입의 2단계 처리
                        - 토큰 발급과 갱신
                        """)
                .version("v1.0.0");

    }
}
