package com.lionkit.mogumarket.search.service;

import com.lionkit.mogumarket.market.entity.Market;
import com.lionkit.mogumarket.search.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisSearchRankService redisSearchRankService;

    public List<ProductDocument> search(String keyword, Long marketId) {
        // 기본 텍스트 조건 (name OR description)
        Criteria text = new Criteria("name").matches(keyword)
                .or(new Criteria("description").matches(keyword));

        // 마켓 범위 제한
        Criteria criteria = (marketId == null)
                ? text
                : text.and(new Criteria("marketId").is(marketId));  // ← 필드명 확인!

        Query query = new CriteriaQuery(criteria);

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(query, ProductDocument.class);

        if (!hits.isEmpty()) {
            redisSearchRankService.increaseKeywordScore(keyword);
        }

        return hits.stream().map(SearchHit::getContent).toList();
    }

    public List<String> getTopTrendingKeywords() {
        return redisSearchRankService.getTopKeywords(10);
    }
}
