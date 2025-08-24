package com.lionkit.mogumarket.payment.portone.config;



import io.portone.sdk.server.PortOneClient;
import io.portone.sdk.server.webhook.WebhookVerifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PortOne 공식 JVM Server SDK 기반 설정
 */
@Configuration
@EnableConfigurationProperties(PortoneProps.class)
public class PortoneSdkConfig {

    /**
     * PortOne API 클라이언트
     * 공식 시그니처: new PortOneClient(apiSecret, apiBase=?, storeId=?)
     * (apiBase 기본값은 https://api.portone.io) :contentReference[oaicite:1]{index=1}
     */
    @Bean(destroyMethod = "close")
    public PortOneClient portOneClient(PortoneProps props) {
        return new PortOneClient(
                props.getApiSecret(),
                props.getApiBase(),
                props.getStoreId()
        );
    }

    /**
     * 웹훅 서명 검증기
     * 공식 사용 예시: new WebhookVerifier(webhookSecret) 로 생성 후
     * verify(signature, timestamp, uniqueId, body) 호출. :contentReference[oaicite:2]{index=2}
     */
    @Bean
    public WebhookVerifier portOneWebhookVerifier(PortoneProps props) {
        return new WebhookVerifier(props.getWebhookSecret());
    }
}
