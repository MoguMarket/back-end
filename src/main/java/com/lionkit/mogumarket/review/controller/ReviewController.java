package com.lionkit.mogumarket.review.controller;


import com.lionkit.mogumarket.review.dto.request.ReviewCreateRequest;
import com.lionkit.mogumarket.review.dto.request.ReviewUpdateRequest;
import com.lionkit.mogumarket.review.dto.response.RatingSummaryResponse;
import com.lionkit.mogumarket.review.dto.response.ReviewResponse;
import com.lionkit.mogumarket.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Tag(name = "리뷰 API", description = "상품 리뷰 등록/조회/수정/삭제 및 별점 요약")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "리뷰 등록(사용자당 상품 1개)")
    public ResponseEntity<Long> create(@RequestBody ReviewCreateRequest request) {
        return ResponseEntity.ok(reviewService.create(request));
    }

    @PatchMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정(본인 소유)")
    public ResponseEntity<Void> update(
            @PathVariable Long reviewId,
            @RequestParam Long userId,
            @RequestBody ReviewUpdateRequest request
    ) {
        reviewService.update(reviewId, userId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 삭제(본인 소유)")
    public ResponseEntity<Void> delete(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {
        reviewService.delete(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "상품별 리뷰 목록 조회(페이징)")
    public ResponseEntity<Page<ReviewResponse>> listByProduct(
            @RequestParam Long productId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(reviewService.listByProduct(productId, page, size));
    }

    @GetMapping("/summary")
    @Operation(summary = "상품별 별점 요약(평균, 개수)")
    public ResponseEntity<RatingSummaryResponse> summary(@RequestParam Long productId) {
        return ResponseEntity.ok(reviewService.summary(productId));
    }
}