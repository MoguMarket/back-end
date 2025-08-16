package com.lionkit.mogumarket.global.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DotenvPropertySourceConfig {

    @PostConstruct
    public void loadEnv() {
        // 프로젝트 루트의 .env 읽기 (없어도 에러 내지 않음)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        int setCount = 0;
        for (var entry : dotenv.entries()) {
            // 이미 OS 환경변수나 JVM -D로 들어온 값이 있으면 덮어쓰지 않음
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
                setCount++;
            }
        }
        log.info("[Dotenv] loaded {} entries from .env", setCount);
    }
}