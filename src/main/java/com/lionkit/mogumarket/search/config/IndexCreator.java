package com.lionkit.mogumarket.search.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@ConditionalOnProperty(
        name = "index.creator.enabled",
        havingValue = "true",            // true일 때만 빈 등록
        matchIfMissing = true            // 설정 없으면 true로 간주 (디폴트 true -> 기본적으론 elasticsearch 실행됨)
)
@Component
@RequiredArgsConstructor
public class IndexCreator {

    private final RestHighLevelClient elasticsearchClient;

    @PostConstruct
    public void createIndexWithMapping() throws IOException {
        String indexName = "product_index";

        // 인덱스 존재 여부 확인
        GetIndexRequest getRequest = new GetIndexRequest(indexName);
        boolean exists = elasticsearchClient.indices().exists(getRequest, RequestOptions.DEFAULT);

        if (!exists) {
            // 매핑 + 설정 JSON 정의
            String mappingJson = """
            {
              "settings": {
                "analysis": {
                  "tokenizer": {
                    "nori_tokenizer_custom": {
                      "type": "nori_tokenizer",
                      "decompound_mode": "mixed"
                    }
                  },
                  "analyzer": {
                    "nori_analyzer": {
                      "type": "custom",
                      "tokenizer": "nori_tokenizer_custom"
                    }
                  }
                }
              },
              "mappings": {
                "properties": {
                  "name": {
                    "type": "text",
                    "analyzer": "nori_analyzer"
                  },
                  "description": {
                    "type": "text",
                    "analyzer": "nori_analyzer"
                  }
                }
              }
            }
            """;

            // 인덱스 생성 요청
            CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
            createRequest.source(mappingJson, XContentType.JSON);

            // 인덱스 생성 실행
            elasticsearchClient.indices().create(createRequest, RequestOptions.DEFAULT);
            System.out.println("✅ Index created: " + indexName);
        } else {
            System.out.println("⚠️ Index already exists: " + indexName);
        }
    }
}