package com.lionkit.mogumarket.global.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class DotenvConfig implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // .env 로드 (없어도 에러 안 나게 설정)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        Map<String, Object> props = new HashMap<>();
        int setCount = 0;

        for (var entry : dotenv.entries()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Spring Environment에 넣기 위해 Map에 추가
            props.put(key, value);

            // OS 환경변수나 JVM -D가 없을 때만 System.setProperty로 설정
            if (System.getProperty(key) == null && System.getenv(key) == null) {
                System.setProperty(key, value);
                setCount++;
            }
        }

        // Spring Environment의 최우선 순위로 추가
        environment.getPropertySources().addFirst(new MapPropertySource("dotenv", props));

        log.info("[Dotenv] loaded {} entries from .env", setCount);
    }
}