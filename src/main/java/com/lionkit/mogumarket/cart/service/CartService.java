package com.lionkit.mogumarket.cart.service;

import com.lionkit.mogumarket.cart.dto.reaponse.CartItemResponse;
import com.lionkit.mogumarket.cart.entity.Cart;
import com.lionkit.mogumarket.cart.repository.CartRepository;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.product.service.ProductStageService;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductStageService productStageService; // ★ 단계 할인 반영

    @Transactional
    public void add(Long userId, Long productId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));

        Cart cart = cartRepository.findByUserIdAndProductId(userId, productId)
                .map(existing -> {
                    existing.increase(quantity);
                    return existing;
                })
                .orElseGet(() -> {
                    User userRef = userRepository.getReferenceById(userId);
                    return Cart.builder()
                            .user(userRef)
                            .product(product)
                            .quantity(quantity)
                            .build();
                });

        cartRepository.save(cart);
    }

    public List<CartItemResponse> list(Long userId) {
        // CartRepository.findAllByUserId 는 @EntityGraph(attributePaths = "product") 혹은 join fetch 전제
        return cartRepository.findAllByUserId(userId).stream()
                .map(c -> {
                    Product p = c.getProduct();

                    // 단계 할인 반영된 단가 (원 단위 long)
                    long unitPrice = calcUnitPriceKRW(p);
                    long lineTotal = unitPrice * c.getQuantity();

                    // DTO가 int라면 변환 (오버플로 주의 → 가능하면 DTO도 long 권장)
                    return new CartItemResponse(
                            p.getId(),
                            p.getName(),
                            Math.toIntExact(unitPrice),
                            c.getQuantity(),
                            Math.toIntExact(lineTotal)
                    );
                })
                .toList();
    }

    /** 단계 할인 반영 단가 (원 단위) */
    private long calcUnitPriceKRW(Product p) {
        return productStageService.getAppliedUnitPrice(p);
    }

    @Transactional
    public void updateQuantity(Long userId, Long productId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");

        Cart cart = cartRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니에 해당 상품이 없습니다."));

        cart.changeQuantity(quantity);
        cartRepository.save(cart);
    }

    @Transactional
    public void remove(Long userId, Long productId) {
        // 경합 상황 고려 시 존재 확인 없이 바로 삭제 호출해도 무해
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clear(Long userId) {
        cartRepository.deleteAllByUserId(userId);
    }
}