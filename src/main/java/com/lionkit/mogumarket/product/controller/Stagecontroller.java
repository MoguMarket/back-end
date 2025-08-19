package com.lionkit.mogumarket.product.controller;

import com.lionkit.mogumarket.product.dto.response.StageResponse;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.entity.ProductStage;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.product.service.ProductStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/")
public class Stagecontroller {
    private final ProductStageService productStageService;
    private final ProductRepository productRepository;

    /** 현재 단계 조회 */
    @GetMapping("/current")
    public ResponseEntity<StageResponse> getCurrentStage(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        ProductStage current = productStageService.getCurrentStage(product);
        return ResponseEntity.ok(StageResponse.fromEntity(current));
    }

    /** 다음 단계 조회 */
    @GetMapping("/next")
    public ResponseEntity<StageResponse> getNextStage(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        ProductStage next = productStageService.getNextStage(product);
        return ResponseEntity.ok(StageResponse.fromEntity(next));
    }

    /** 다음 단계까지 남은 수량 */
    @GetMapping("/remaining")
    public ResponseEntity<Double> getRemainingToNext(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        Double remaining = productStageService.remainingToNext(product);
        return ResponseEntity.ok(remaining);
    }

    /** 현재 단계 기준 적용 단가 */
    @GetMapping("/price")
    public ResponseEntity<Double> getAppliedUnitPrice(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        double price = productStageService.getAppliedUnitPrice(product);
        return ResponseEntity.ok(price);
    }
}

