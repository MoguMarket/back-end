package com.lionkit.mogumarket.search.service;

import com.lionkit.mogumarket.search.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import javax.annotation.Nullable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisSearchRankService redisSearchRankService;

    public List<ProductDocument> search(String keyword) {

        Criteria criteria = new Criteria("name").matches(keyword)
                .or(new Criteria("description").matches(keyword));

        Query query = new CriteriaQuery(criteria);

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(query, ProductDocument.class);

        if (!hits.isEmpty()) redisSearchRankService.increaseKeywordScore(keyword); // 검색 성공 후 up


        return hits.stream().map(SearchHit::getContent).toList();
    }

    public List<String> getTopTrendingKeywords() {
        return redisSearchRankService.getTopKeywords(10);
    }
}
