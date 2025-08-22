package com.lionkit.mogumarket.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean(name = "odcloud")
    public WebClient odcloud(WebClient.Builder builder,
                             @Value("${odcloud.base-url:https://api.odcloud.kr}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
