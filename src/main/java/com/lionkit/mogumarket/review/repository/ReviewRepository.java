package com.lionkit.mogumarket.review.repository;

import com.lionkit.mogumarket.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProduct_Id(Long productId, Pageable pageable);

    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);

    @Query("select avg(r.rating) from Review r where r.product.id = :productId")
    Double findAverageRatingByProductId(Long productId);

    @Query("select count(r) from Review r where r.product.id = :productId")
    Long countByProductId(Long productId);
}