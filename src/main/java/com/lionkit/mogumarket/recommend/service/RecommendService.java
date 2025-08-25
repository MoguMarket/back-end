package com.lionkit.mogumarket.recommend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyStageRepository;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.recommend.dto.request.ListingReviewRequest;

import com.lionkit.mogumarket.recommend.dto.response.ListingReviewResponse;
import com.lionkit.mogumarket.recommend.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final LlmClient llmClient;
    private final ProductRepository productRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyStageRepository stageRepository;
    private final ObjectMapper om = new ObjectMapper();

    @Transactional(readOnly = true)
    public ListingReviewResponse reviewListing(ListingReviewRequest r) {
        final String currency = (r.getCurrency() == null || r.getCurrency().isBlank()) ? "KRW" : r.getCurrency();

        // productId가 오면 DB 값으로 일부 보정(누락 필드 채우기)
        if (r.getProductId() != null) {
            productRepository.findWithStoreAndMarketById(r.getProductId()).ifPresent(p -> {
                if (r.getName() == null || r.getName().isBlank()) r.setName(p.getName());
                if (r.getDescription() == null || r.getDescription().isBlank()) r.setDescription(p.getDescription());
                if (r.getUnit() == null) r.setUnit(p.getUnit());
                if (r.getOriginalPricePerBaseUnit() <= 0) r.setOriginalPricePerBaseUnit(p.getOriginalPricePerBaseUnit());
                if (r.getStock() <= 0) r.setStock(p.getStock());
            });
        }

        // LLM 프롬프트
        String prompt = buildListingReviewPrompt(r, currency);

        try {
            String raw = llmClient.complete(prompt);

            // 기대 JSON 스키마
            // {
            //   "recommendedPrice": number,
            //   "minRecommendedPrice": number,
            //   "maxRecommendedPrice": number,
            //   "reasoning": string,   // 최대 2줄
            //   "suggestions": [
            //     {"field":"name","suggestedValue":"...","reason":"..."},
            //     ...
            //   ]
            // }

            JsonNode root = om.readTree(raw);

            double price = root.path("recommendedPrice").asDouble();
            double min   = root.path("minRecommendedPrice").asDouble(price);
            double max   = root.path("maxRecommendedPrice").asDouble(price);
            String reasoning = trim2Lines(root.path("reasoning").asText(""));

            List<ListingReviewResponse.FieldSuggestion> suggestions = new ArrayList<>();
            if (root.has("suggestions") && root.get("suggestions").isArray()) {
                for (JsonNode s : root.get("suggestions")) {
                    suggestions.add(
                            ListingReviewResponse.FieldSuggestion.builder()
                                    .field(s.path("field").asText(""))
                                    .suggestedValue(s.path("suggestedValue").asText(""))
                                    .reason(trim2Lines(s.path("reason").asText("")))
                                    .build()
                    );
                }
            }

            if (Double.isNaN(price) || price <= 0) throw new IllegalArgumentException("Invalid LLM price");

            return ListingReviewResponse.builder()
                    .recommendedPrice(round(price))
                    .minRecommendedPrice(round(min))
                    .maxRecommendedPrice(round(max))
                    .reasoning(reasoning)
                    .currency(currency)
                    .fromLlm(true)
                    .suggestions(suggestions)
                    .build();

        } catch (Exception e) {
            // 폴백: 기존 로직 간략화 + 기본 제안 몇 개 생성
            double cost = r.getOriginalPricePerBaseUnit();
            double targetMargin = 0.25;
            double candidate = Math.max(1d, cost * (1 + targetMargin));

            List<ListingReviewResponse.FieldSuggestion> fallback = List.of(
                    ListingReviewResponse.FieldSuggestion.builder()
                            .field("name")
                            .suggestedValue(r.getName())
                            .reason("핵심 키워드(원산지/중량)를 앞쪽에 배치하면 검색/가독성이 좋아요.")
                            .build(),
                    ListingReviewResponse.FieldSuggestion.builder()
                            .field("description")
                            .suggestedValue(r.getDescription())
                            .reason("첫 문단에 효익 1줄 요약, 두 번째 줄에 보관/신선도 팁을 넣어보세요.")
                            .build()
            );

            return ListingReviewResponse.builder()
                    .recommendedPrice(round(candidate))
                    .minRecommendedPrice(round(candidate * 0.92))
                    .maxRecommendedPrice(round(candidate * 1.08))
                    .reasoning("LLM 실패 폴백: 원가+고정마진 기준\n필드 제안은 기본 가이드 제공")
                    .currency(currency)
                    .fromLlm(false)
                    .suggestions(fallback)
                    .build();
        }
    }


    private String buildListingReviewPrompt(ListingReviewRequest r, String currency) {
        return """
        당신은 전통시장 ‘상품 등록’ 컨설턴트이자 가격 분석가입니다.
        아래 입력값을 바탕으로 **가격 추천**과 **필드별 수정 제안**을 생성하세요.
        반드시 **아래 JSON 스키마만** 반환하고, JSON 외 텍스트는 금지합니다.
        각 reason은 최대 2줄을 넘기지 마세요.

        출력 JSON 스키마:
        {
          "recommendedPrice": number,
          "minRecommendedPrice": number,
          "maxRecommendedPrice": number,
          "reasoning": string,
          "suggestions": [
            {"field":"description","suggestedValue":string},
            {"field":"originalPricePerBaseUnit","suggestedValue":string},
            {"field":"stock","suggestedValue":string},
            {"field":"maxDiscountPercent","suggestedValue":string},
            {"field":"stageCount","suggestedValue":string}
          ]
        }

        입력 데이터:
        - 상품명: %s
        - 설명: %s
        - 단위: %s
        - 정가/원가(기준단위당): %.4f
        - 재고(기준단위): %.2f
        - 최대 할인율(%%): %s
        - 단계 수: %s
        - 통화: %s

        규칙:
        1) 가격은 원가, 재고, 할인정책(최대할인/단계수)을 고려해 min~max와 대표가를 산출하세요.
        2) suggestions의 suggestedValue는 사용자가 그대로 복붙해도 되는 간결한 값으로.
        3) 모든 금액은 %s 기준.
        """.formatted(
                ns(r.getName()),
                ns(r.getDescription()),
                r.getUnit() == null ? "UNKNOWN" : r.getUnit().name(),
                r.getOriginalPricePerBaseUnit(),
                r.getStock(),
                r.getMaxDiscountPercent() == null ? "N/A" : r.getMaxDiscountPercent().toString(),
                r.getStageCount() == null ? "N/A" : r.getStageCount().toString(),
                currency,
                currency
        );
    }

    private String trim2Lines(String s) {
        if (s == null) return "";
        String[] lines = s.split("\\R");
        String joined = (lines.length <= 2) ? String.join("\n", lines) : (lines[0] + "\n" + lines[1]);
        return joined.trim();
    }

    private String buildPromptFromEntities(Product p, GroupBuy gb, String currency) {
        // GroupBuy 정보(있으면) 추출
        String gbOpen = (gb != null) ? "YES" : "NO";
        String maxDiscount = (gb != null) ? String.valueOf(gb.getMaxDiscountPercent()) : "N/A";
        String targetQty   = (gb != null) ? String.valueOf(gb.getTargetQty()) : "N/A";
        String currentQty  = (gb != null) ? String.valueOf(gb.getCurrentQty()) : "N/A";
        String stageCount  = (gb != null) ? String.valueOf(stageRepository.findByGroupBuyOrderByStartQtyAsc(gb).size()) : "N/A";

        // Product 정보
        String marketName = (p.getStore() != null && p.getStore().getMarket() != null)
                ? p.getStore().getMarket().getName() : "";
        double avgRating = p.getReviews().isEmpty()
                ? 0.0
                : p.getReviews().stream().mapToDouble(r -> r.getRating() == null ? 0 : r.getRating()).average().orElse(0.0);

        return """
            당신은 전통시장 상인을 돕는 전문 가격 분석가입니다.
            아래 상품/공구 데이터를 바탕으로 **공동구매(공구) 등록 시 적정 가격대**를 제시하세요.
            반드시 아래 JSON 스키마만 반환하고, JSON 외 텍스트는 금지합니다.

            출력 JSON 스키마:
            {
              "recommendedPrice": number,
              "minRecommendedPrice": number,
              "maxRecommendedPrice": number,
              "reasoning": string
            }

            입력 데이터:
            - 시장명: %s
            - 상품명: %s
            - 카테고리: %s
            - 단위(기준단위): %s
            - 정가/원가(기준단위당): %.4f
            - 총 재고(기준단위): %.2f
            - 누적 판매/주문 수량(기준단위): %.2f
            - 평균 별점(0~5): %.2f
            - 공구 진행 중 여부: %s
            - (공구) 최대 할인율(%%): %s
            - (공구) 목표 수량: %s
            - (공구) 현재 누적 수량: %s
            - (공구) 단계 수: %s
            - 통화: %s

            규칙:
            1) 원가+마진, 과거 판매/리뷰 신뢰도, 공구(할인/목표/단계) 정보를 종합해 가격대를 산출합니다.
            2) 최저가는 참여 유도(공격적), 최고가는 수익 확보(보수적) 관점에서 제시합니다.
            3) 모든 금액은 입력 통화(%s)로 제시합니다.
            4) 반드시 위 JSON만 출력하세요.
            """.formatted(
                ns(marketName),
                ns(p.getName()),
                p.getCategory() == null ? "" : p.getCategory().name(),
                p.getUnit() == null ? "UNKNOWN" : p.getUnit().name(),
                p.getOriginalPricePerBaseUnit(),
                p.getStock(),
                p.getCurrentBaseQty(),
                avgRating,
                gbOpen,
                maxDiscount,
                targetQty,
                currentQty,
                stageCount,
                currency,
                currency
        );
    }

    private double round(double v) { return Math.round(v); }
    private String ns(String s){ return s==null? "": s; }
}