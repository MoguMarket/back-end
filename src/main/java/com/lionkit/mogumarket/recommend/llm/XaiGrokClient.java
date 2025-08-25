package com.lionkit.mogumarket.recommend.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class XaiGrokClient implements LlmClient {

    @Value("${xai.api.key:}")
    private String apiKey;

    @Value("${xai.model:grok-3}")
    private String model;

    @Value("${xai.base-url:https://api.x.ai/v1}")
    private String baseUrl;

    private final ObjectMapper om = new ObjectMapper();

    private RestTemplate buildRestTemplate() {
        var factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setConnectTimeout((int) Duration.ofSeconds(30).toMillis());
        return new RestTemplate(factory);
    }

    @Override
    public String complete(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("xAI API 키가 설정되어 있지 않습니다. (xai.api.key)");
        }

        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "당신은 시장 상인을 위한 전문 가격 분석가입니다. " +
                                        "반드시 아래 JSON 스키마만 출력하세요."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        String reqJson;
        try {
            reqJson = om.writerWithDefaultPrettyPrinter().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            reqJson = body.toString();
        }

        log.info("[XaiGrokClient] URL: {}", url);
        log.info("[XaiGrokClient] Request Body:\n{}", reqJson);

        try {
            RestTemplate rest = buildRestTemplate();
            ResponseEntity<String> res = rest.postForEntity(url, new HttpEntity<>(body, headers), String.class);

            log.info("[XaiGrokClient] Response Status: {}", res.getStatusCodeValue());
            log.info("[XaiGrokClient] Response Body:\n{}", res.getBody());

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                throw new RuntimeException("xAI Grok 응답 비정상: " + res.getStatusCode());
            }

            // JSON 파싱해서 content 추출
            Map<?, ?> root = om.readValue(res.getBody(), Map.class);
            List<?> choices = (List<?>) root.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("xAI Grok 응답에 choices가 없습니다.");
            }
            Object first = choices.get(0);
            if (!(first instanceof Map)) {
                throw new RuntimeException("xAI Grok choices[0] 형식 오류");
            }
            Map<?, ?> choice0 = (Map<?, ?>) first;
            Map<?, ?> message = (Map<?, ?>) choice0.get("message");
            if (message == null) {
                throw new RuntimeException("xAI Grok message가 없습니다.");
            }
            Object content = message.get("content");
            if (content == null) {
                throw new RuntimeException("xAI Grok content가 없습니다.");
            }
            return content.toString();

        } catch (HttpStatusCodeException e) {
            // 4xx/5xx 에러 바디까지 로깅
            log.error("[XaiGrokClient] HTTP {} Error Body:\n{}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new RuntimeException("xAI Grok 호출 실패: HTTP " + e.getRawStatusCode(), e);
        } catch (Exception e) {
            log.error("[XaiGrokClient] 호출 중 예외", e);
            throw new RuntimeException("xAI Grok 호출 실패: " + e.getMessage(), e);
        }
    }
}