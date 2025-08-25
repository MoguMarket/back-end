package com.lionkit.mogumarket.search.repository;

import com.lionkit.mogumarket.search.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {
}