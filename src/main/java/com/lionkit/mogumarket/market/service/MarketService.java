package com.lionkit.mogumarket.market.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

    private final WebClient odcloud;

    @Value("${odcloud.service.key:${ODCLOUD_SERVICE_KEY:}}")
    private String serviceKeyRaw;

    private String serviceKey;

    @PostConstruct
    public void init() {
        this.serviceKey = sanitizeKey(serviceKeyRaw);
        log.info("[ODCLOUD] serviceKey loaded at startup: {}", mask(this.serviceKey));

        String env = System.getenv("ODCLOUD_SERVICE_KEY");
        log.info("[ODCLOUD] env ODCLOUD_SERVICE_KEY present? {}", (env != null && !env.isBlank()));
    }

    public Mono<Map<String, Object>> fetch(int page, int perPage) {
        return fetch(page, perPage, null, null, null);
    }

    public Mono<Map<String, Object>> fetch(int page, int perPage, String sido, String sigungu, String marketNameLike) {
        if (serviceKey == null || serviceKey.isBlank()) {
            return Mono.error(new IllegalStateException(
                    "ODCLOUD serviceKey is null/blank. Check application.yml (odcloud.service.key) or env ODCLOUD_SERVICE_KEY."));
        }

        String normalized = normalizeKey(serviceKey);
        String encodedForQuery = encodeKeyForQuery(normalized);

        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl("https://api.odcloud.kr")
                .path("/api/15052837/v1/uddi:1fd54eb7-0565-4755-8ec7-a70931b6dc77")
                .queryParam("page", page)
                .queryParam("perPage", perPage)
                .queryParam("serviceKey", encodedForQuery);

        // ✅ 지역 필터 추가 (Key는 미리 인코딩된 문자열 사용)
        if (sido != null && !sido.isBlank()) {
            b.queryParam(
                "cond%5B%EC%8B%9C%EB%8F%84::EQ%5D",
                URLEncoder.encode(sido.trim(), StandardCharsets.UTF_8)
            );
        }
        if (sigungu != null && !sigungu.isBlank()) {
            b.queryParam(
                "cond%5B%EC%8B%9C%EA%B5%B0%EA%B5%AC::EQ%5D",
                URLEncoder.encode(sigungu.trim(), StandardCharsets.UTF_8)
            );
        }
        if (marketNameLike != null && !marketNameLike.isBlank()) {
            b.queryParam(
                "cond%5B%EC%8B%9C%EC%9E%A5%EB%AA%85::LIKE%5D",
                URLEncoder.encode(marketNameLike.trim(), StandardCharsets.UTF_8)
            );
        }

        var uri = b.build(true).toUri();
        log.info("[ODCLOUD][A] final URI = {}", uri);

        return odcloud.get()
                .uri(uri)
                .retrieve()
                .onStatus(s -> !s.is2xxSuccessful(), resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("<empty>")
                                .flatMap(body -> {
                                    String msg = "ODCLOUD error. status=" + resp.statusCode() + ", body=" + body;
                                    log.error(msg);
                                    return Mono.error(new ResponseStatusException(resp.statusCode(), msg));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    private String sanitizeKey(String in) {
        if (in == null) return null;
        String k = in.replace("\uFEFF", "").trim();
        if ((k.startsWith("\"") && k.endsWith("\"")) || (k.startsWith("'") && k.endsWith("'"))) {
            k = k.substring(1, k.length() - 1);
        }
        return k;
    }

    private String normalizeKey(String key) {
        if (key == null) return null;
        String lower = key.toLowerCase();
        if (lower.contains("%2b") || lower.contains("%2f") || lower.contains("%3d")) {
            try {
                String decoded = URLDecoder.decode(key, StandardCharsets.UTF_8);
                log.info("[ODCLOUD] detected encoded key → decoded once (head={})", mask(decoded));
                return decoded;
            } catch (Exception e) {
                log.warn("[ODCLOUD] key decode failed, using raw key. reason={}", e.getMessage());
                return key;
            }
        }
        return key;
    }

    private String encodeKeyForQuery(String key) {
        if (key == null) return null;
        return key.replace("+", "%2B")
                .replace("=", "%3D");
    }

    private String mask(String key) {
        if (key == null || key.isBlank()) return "<blank>";
        return key.length() <= 6 ? key + "****" : key.substring(0, 6) + "****";
    }
}