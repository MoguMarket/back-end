package com.lionkit.mogumarket.recommend.controller;


import com.lionkit.mogumarket.recommend.dto.request.ListingReviewRequest;
import com.lionkit.mogumarket.recommend.dto.response.ListingReviewResponse;
import com.lionkit.mogumarket.recommend.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recommend", description = "AI 가격 추천 API")
@RequestMapping(value = "/api/recommend", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @PostMapping("/listing/review")
    @Operation(
            summary = "상품 등록값 리뷰(가격+필드수정 제안)",
            description = "입력한 상품 등록 값으로 가격대를 추천하고, 각 필드의 수정 제안을 반환합니다."
    )
    public ResponseEntity<ListingReviewResponse> review(@Valid @RequestBody ListingReviewRequest req) {
        return ResponseEntity.ok(recommendService.reviewListing(req));
    }

    @GetMapping("/health")
    public String health() { return "ok"; }
}