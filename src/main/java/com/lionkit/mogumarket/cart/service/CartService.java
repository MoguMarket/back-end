package com.lionkit.mogumarket.cart.service;

import com.lionkit.mogumarket.cart.dto.request.CartLineUpsertRequest;
import com.lionkit.mogumarket.cart.dto.response.CartLineResponse;
import com.lionkit.mogumarket.cart.dto.request.CartBulkSetRequest;
import com.lionkit.mogumarket.cart.dto.response.CartSummaryResponse;
import com.lionkit.mogumarket.cart.entity.Cart;
import com.lionkit.mogumarket.cart.entity.CartLine;
import com.lionkit.mogumarket.cart.enums.PurchaseRoute;
import com.lionkit.mogumarket.cart.repository.CartLineRepository;
import com.lionkit.mogumarket.cart.repository.CartRepository;
import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyStageRepository;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartLineRepository cartLineRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private final GroupBuyStageRepository stageRepository;

    /**
     * cart 를 생성합니다
     * 유저와 1:1 관계를 가지며, 기본적으로 사용자의 회원가입과 함께 cart 를 생성시킵니다.
     * @param userId
     * @return
     */
    public Cart createOrGetCart(Long userId) {
        return cartRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow(()->new BusinessException(ExceptionType.USER_NOT_FOUND));
                    Cart cart = Cart.builder().user(user).build();
                    return cartRepository.save(cart);
                });
    }

    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.CART_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Cart getCartById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.CART_NOT_FOUND));
    }

    /**
     * 장바구니 라인 목록(+예상 금액)
     */
    @Transactional(readOnly = true)
    public List<CartLineResponse> list(Long userId) {
        List<CartLine> lines = cartLineRepository.findAllWithProductAndGroupBuyByCartUserId(userId);
        return lines.stream()
                .map(line -> CartLineResponse.of(line, calcApplicableUnitPrice(line)))
                .toList();
    }

    /**
     * CartLine 하나에 대해서만 생성/업데이트 합니다.
     * - qtyBase == 0 : 삭제
     * - 존재O & qtyBase>0 : 절대값 변경
     */
    public List<CartLineResponse>  addOrUpdateCartLine(Long userId, CartLineUpsertRequest request) {
        if (request.getQtyBase() < 0) throw new BusinessException(ExceptionType.INVALID_QTYBASE);

        Cart cart = createOrGetCart(userId);
        Product p = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));

        /**
         * 변경 수량이 0
         * - 기존 장바구니 라인이 있으면 삭제
         * - 없으면 아무 작업도 하지 않음
         */
        if (request.getQtyBase() == 0d) {
            cartLineRepository.deleteByCart_User_IdAndProduct_IdAndRoute(userId, request.getProductId(), request.getRoute());
            return list(userId);
        }

        CartLine line = cartLineRepository
                .findByCart_User_IdAndProduct_IdAndRoute(userId, request.getProductId(), request.getRoute())
                .orElse(null);

        if (line == null) {
            /**
             * 변경 수량 >  0 && 기존 라인 없음
             * - 새로운 CartLine을 생성
             * - route 값은 그대로 유지
             */
            line = CartLine.builder()
                    .cart(cart)
                    .product(p)
                    .orderedBaseQty(request.getQtyBase())
                    .route(request.getRoute())
                    .build();
            cart.addLine(line);
            cartLineRepository.save(line);
        } else {
            /**
             * 변경 수량 >  0 && 기존 라인 있음
             * - 기존 라인의 수량(orderedBaseQty)을 변경
             * - route 값은 그대로 유지
             */
            line.change(request.getQtyBase());
        }

        return list(userId);

    }




    /**
     * CartLine 여러개에 대한 생성/업데이트를 진행합니다.
     */
    public List<CartLineResponse> bulkSet(Long userId, CartBulkSetRequest req) {
        Cart cart = createOrGetCart(userId);

        // 1) 요청 병합 : 상품ID & <route, qty>  형태의 맵을 생성
        Map<Long, Map<PurchaseRoute, Double>> desired = new LinkedHashMap<>();
        for (CartBulkSetRequest.Line l : req.lines()) {
            if (l.qtyBase() < 0) throw new BusinessException(ExceptionType.INVALID_QTYBASE);
            desired
                    .computeIfAbsent(l.productId(), k -> new EnumMap<>(PurchaseRoute.class))
                    .put(l.route(), l.qtyBase()); // 중복 오면 마지막 값 우선(정책)
        }



        // 2) 제품 일괄 유효성 검증
        List<Long> productIds = new ArrayList<>(desired.keySet());
        Map<Long, Product> productById = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, it -> it));
        if (productById.size() != desired.size()) {
            throw new BusinessException(ExceptionType.PRODUCT_NOT_FOUND);
        }

        // 3)  상품ID & CartLine 형태의 맵을 생성
        List<CartLine> existing = cartLineRepository.findAllByCart_User_Id(userId);
        Map<Long, Map<PurchaseRoute, CartLine>> existingMap = new HashMap<>();
        for (CartLine cl : existing) {
            existingMap
                    .computeIfAbsent(cl.getProduct().getId(), k -> new EnumMap<>(PurchaseRoute.class))
                    .put(cl.getRoute(), cl);
        }


        // 4) 적용
        for (Map.Entry<Long, Map<PurchaseRoute, Double>> e : desired.entrySet()) {
            Long productId = e.getKey();
            Product p = productById.get(productId);

            Map<PurchaseRoute, Double> routes = e.getValue();
            Map<PurchaseRoute, CartLine> curByRoute =
                    existingMap.getOrDefault(productId, Collections.emptyMap());

            for (Map.Entry<PurchaseRoute, Double> r : routes.entrySet()) {
                PurchaseRoute route = r.getKey();
                double qty = r.getValue();
                CartLine cur = curByRoute.get(route);

                if (qty == 0d) {                 // 삭제
                    if (cur != null) cartLineRepository.delete(cur);
                    continue;
                }

                if (cur == null) {                // 신규
                    CartLine line = CartLine.builder()
                            .cart(cart)
                            .product(p)
                            .orderedBaseQty(qty)
                            .route(route)
                            .build();
                    cart.addLine(line);
                    cartLineRepository.save(line);
                } else {                          // 변경
                    cur.change(qty);
                }
            }
        }

        return list(userId);
    }



    /**
     * 현재 적용 단가 계산
     * - 공구 상품이면 현재 단계의 appliedUnitPrice 사용(스냅샷 기반)
     * - 일반 구매 상품이면 일반가(originalPricePerBaseUnit) 사용
     */
    private double calcApplicableUnitPrice(CartLine line) {
        Product p = line.getProduct();

        // NORMAL(즉시구매): 일반가
        if (line.getRoute() == PurchaseRoute.NORMAL) return p.getOriginalPricePerBaseUnit();


        // GROUPBUY: 현재 스테이지 스냅샷 단가
        GroupBuy gb = p.getGroupBuy(); // 상품 1:1 공구 (생성시 필수 정책)
        if (gb == null) return p.getOriginalPricePerBaseUnit();             // 정책에선 없다고 보지 않지만, 방어 로직: 공구가 없다면 일반가로 폴백



        double curQty = gb.getCurrentQty();
        GroupBuyStage cur = stageRepository
                .findTopByGroupBuyAndStartQtyLessThanEqualOrderByStartQtyDesc(gb, curQty)
                .orElse(null);

        if (cur != null) return cur.getAppliedUnitPrice();

        // 정책에선 없다고 보지 않지만, 방어 로직: 공구가 없다면 공구 원가로 폴백
        return gb.getBasePricePerBaseUnitSnapshot();
    }

    /** 단일 라인 제거 */
    public void remove(Long userId, Long productId, PurchaseRoute route) {
        cartLineRepository.deleteByCart_User_IdAndProduct_IdAndRoute(userId, productId,route);
    }

    /** 장바구니 비우기 (유저의 모든 라인 제거) */
    public void clear(Long userId) {
        cartLineRepository.deleteAllByCart_User_Id(userId);
    }

    @Transactional(readOnly = true)
    public CartSummaryResponse summary(Long userId) {
        List<CartLineResponse> items = list(userId); // 여기서 라인별 단가 계산 완료
        return CartSummaryResponse.fromItems(items);
    }

}