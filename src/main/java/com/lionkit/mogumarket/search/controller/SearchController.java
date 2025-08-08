package com.lionkit.mogumarket.search.controller;

import com.lionkit.mogumarket.search.document.ProductDocument;
import com.lionkit.mogumarket.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Tag(name = "Search API", description = "상품 검색(elasticsearch) 관련 API")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(
            summary = "상품 검색",
            description = "키워드를 기반으로 상품을 검색합니다. \n" +
                    "검색어는 상품명, 카테고리명, 브랜드명 등에서 일치하는 항목을 찾습니다."
    )
    public ResponseEntity<List<ProductDocument>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(searchService.search(keyword));
    }

    @GetMapping("/trending")
    @Operation(
            summary = "인기 검색어",
            description = "최근에 많이 검색된 키워드를 반환합니다. \n" +
                    "최대 10개의 키워드를 반환하며, 인기 검색어는 시간에 따라 변동될 수 있습니다."
    )
    public ResponseEntity<List<String>> trending() {
        return ResponseEntity.ok(searchService.getTopTrendingKeywords());
    }
}
