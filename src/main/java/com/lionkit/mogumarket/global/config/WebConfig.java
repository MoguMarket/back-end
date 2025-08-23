package com.lionkit.mogumarket.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins(
						"http://localhost:3000",
						"http://localhost:5173",
						"http://www.jungjiyu.com",
						"https://localhost:3000",
						"https://localhost:5173",
						"https://www.jungjiyu.com"
				)
				.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // OPTIONS 추가
				.allowedHeaders("*")
				.allowCredentials(true); // 쿠키/인증 포함 시 필요
	}

}