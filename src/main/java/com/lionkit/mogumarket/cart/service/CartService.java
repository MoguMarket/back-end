package com.lionkit.mogumarket.cart.service;

import com.lionkit.mogumarket.cart.dto.reaponse.CartItemResponse;
import com.lionkit.mogumarket.cart.entity.Cart;
import com.lionkit.mogumarket.cart.repository.CartRepository;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
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
                    // getReferenceById: 프록시로 가져와서 쿼리 줄임
                    User userRef = userRepository.getReferenceById(userId);
                    return Cart.builder()
                            .user(userRef)
                            .product(product)
                            .quantity(quantity)
                            .build();
                });

        cartRepository.save(cart); // upsert 느낌 (신규면 insert, 기존이면 update)
    }

    public List<CartItemResponse> list(Long userId) {
        return cartRepository.findAllByUserId(userId).stream()
                .map(c -> {
                    Product p = c.getProduct();
                    Integer unitPrice = (p.getDiscountPrice() != null) ? p.getDiscountPrice() : p.getOriginalPrice();
                    int lineTotal = unitPrice * c.getQuantity();
                    return new CartItemResponse(
                            p.getId(),
                            p.getName(),
                            unitPrice,
                            c.getQuantity(),
                            lineTotal
                    );
                })
                .toList();
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
        if (!cartRepository.existsByUserIdAndProductId(userId, productId)) return;
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clear(Long userId) {
        cartRepository.deleteAllByUserId(userId);
    }
}