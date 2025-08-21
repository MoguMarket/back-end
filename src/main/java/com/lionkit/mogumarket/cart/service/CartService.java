package com.lionkit.mogumarket.cart.service;

import com.lionkit.mogumarket.cart.dto.reaponse.CartItemResponse;
import com.lionkit.mogumarket.cart.entity.Cart;
import com.lionkit.mogumarket.cart.repository.CartRepository;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStatus;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyStageRepository;
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

    // ▼ 신규 의존성: 공구 진행 상태 기반 가격 계산
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyStageRepository stageRepository;

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
        return cartRepository.findAllByUserId(userId).stream()
                .map(c -> {
                    Product p = c.getProduct();

                    long unitPrice = calcUnitPriceKRW(p);
                    long lineTotal = unitPrice * c.getQuantity();

                    // DTO가 int 필드라면 변환 (가능하면 DTO도 long 으로 바꾸는 걸 권장)
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

    /** 진행중인 공구가 있으면 단계 할인 적용, 없으면 원가 */
    private long calcUnitPriceKRW(Product p) {
        double original = p.getOriginalPricePerBaseUnit();

        // 최신 OPEN 공구 한 건만 사용 (없으면 원가)
        GroupBuy gb = groupBuyRepository
                .findTopByProductAndStatusOrderByCreatedAtDesc(p, GroupBuyStatus.OPEN)
                .orElse(null);
        if (gb == null) {
            return Math.round(original);
        }

        double totalQty = gb.getCurrentQty(); // 누적 수량(주문 테이블 합산을 따로 할 필요 없이 필드 사용)
        GroupBuyStage cur = stageRepository
                .findTopByGroupBuyAndStartQtyLessThanEqualOrderByStartQtyDesc(gb, totalQty);
        double discount = (cur == null) ? 0.0 : cur.getDiscountPercent();

        double applied = original * ((100.0 - discount) / 100.0);
        return Math.round(applied);
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
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clear(Long userId) {
        cartRepository.deleteAllByUserId(userId);
    }
}