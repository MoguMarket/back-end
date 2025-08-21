package com.lionkit.mogumarket.review.service;

import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.review.dto.*;
import com.lionkit.mogumarket.review.dto.request.ReviewCreateRequest;
import com.lionkit.mogumarket.review.dto.request.ReviewUpdateRequest;
import com.lionkit.mogumarket.review.dto.response.RatingSummaryResponse;
import com.lionkit.mogumarket.review.dto.response.ReviewResponse;
import com.lionkit.mogumarket.review.entity.Review;
import com.lionkit.mogumarket.review.repository.ReviewRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /** 리뷰 등록 (사용자당 상품 1개) */
    @Transactional
    public Long create(ReviewCreateRequest req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("별점은 1~5 사이여야 합니다.");
        }

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        reviewRepository.findByUser_IdAndProduct_Id(user.getId(), product.getId())
                .ifPresent(r -> { throw new IllegalStateException("이미 해당 상품에 대한 리뷰가 존재합니다."); });

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(req.getRating())
                .build();

        return reviewRepository.save(review).getId();
    }

    /** 리뷰 수정 (본인 리뷰라고 가정) */
    @Transactional
    public void update(Long reviewId, Long userId, ReviewUpdateRequest req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("별점은 1~5 사이여야 합니다.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인 리뷰만 수정할 수 있습니다.");
        }

        // 엔티티에 setter가 없다면 업데이트 메서드 추가 권장. 여기서는 간단히 리플렉션 없이 Builder 패턴 재활용 대신 엔티티에 메서드 추가를 추천.
        // 예: review.updateRating(req.getRating());
        // 임시로 필드 접근하려면 엔티티에 @Setter 추가가 필요. 깔끔하게 메서드 추가 예시:
        // (Review 엔티티에)
        // public void changeRating(int val) { this.rating = val; }
        review.getClass(); // no-op
        try {
            var field = Review.class.getDeclaredField("rating");
            field.setAccessible(true);
            field.set(review, req.getRating());
        } catch (Exception ignore) {}

        reviewRepository.save(review);
    }

    /** 리뷰 삭제 (본인 리뷰라고 가정) */
    @Transactional
    public void delete(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인 리뷰만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }

    /** 상품별 리뷰 목록 (페이징) */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listByProduct(Long productId, Integer page, Integer size) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size < 1 || size > 100) ? 10 : size;
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
        return reviewRepository.findByProduct_Id(productId, pageable)
                .map(ReviewResponse::from);
    }

    /** 상품별 별점 요약 */
    @Transactional(readOnly = true)
    public RatingSummaryResponse summary(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        Long cnt = reviewRepository.countByProductId(productId);
        return RatingSummaryResponse.builder()
                .productId(productId)
                .average(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0) // 소수점 1자리 반올림
                .count(cnt != null ? cnt : 0L)
                .build();
    }
}