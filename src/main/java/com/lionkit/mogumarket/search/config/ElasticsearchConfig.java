package com.lionkit.mogumarket.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${ELASTICSEARCH_HOST}")
    private String esHost;

    @Value("${ELASTICSEARCH_PORT}")
    private int esPort;

    @Value("${ELASTICSEARCH_SCHEME}")
    private String esScheme;

    @Bean(name = "customElasticsearchClient")
    public RestHighLevelClient elasticsearchClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, esPort, esScheme)
                )
        );
    }
}