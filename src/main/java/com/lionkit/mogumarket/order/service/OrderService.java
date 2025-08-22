package com.lionkit.mogumarket.order.service;

import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStatus;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyStageRepository;
import com.lionkit.mogumarket.order.entity.OrderLine;
import com.lionkit.mogumarket.order.entity.Orders;
import com.lionkit.mogumarket.order.enums.OrderStatus;
import com.lionkit.mogumarket.order.repository.OrderRepository;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // org.springframework.transaction.annotation.Transactional 로 import!
public class OrderService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // ⬇️ 신규 의존성: 공구/스테이지
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyStageRepository groupBuyStageRepository;

    /**
     * 단일 상품 주문 확정
     */
    public Long confirmSingleProductOrder(Long userId, Long productId, double qtyBase) {
        try {
            // 1) 상품 비관적 락
            Product product = productRepository.findForUpdate(productId)
                    .orElseThrow(() -> new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));

            // 2) 재고/누적 반영 (프로젝트에 맞게 구현되어 있다고 가정)
            product.increaseCurrentBaseQty(qtyBase);

            // 3) 공구/할인/단가 계산
            PriceSnapshot snap = calcPriceSnapshot(product);

            // 4) 주문 헤더/라인 생성
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

            Orders orders = Orders.builder()
                    .user(user)
                    .status(OrderStatus.CONFIRMED)
                    .build();

            OrderLine line = OrderLine.builder()
                    .orders(orders)
                    .product(product)
                    .orderedBaseQty(qtyBase)
                    .levelSnapshot(snap.level)                   // 0이면 공구/스테이지 없음
                    .discountPercentSnapshot(snap.discountPercent)
                    .unitPriceSnapshot(snap.unitPrice)           // 기준단위당 적용 단가(원)
                    .build();

            orders.getLines().add(line); // cascade = ALL 가정

            return orderRepository.save(orders).getId();

        } catch (LockTimeoutException e) {
            throw new BusinessException(ExceptionType.PRODUCT_LOCK_TIMEOUT);
        } catch (PessimisticLockException e) {
            throw new BusinessException(ExceptionType.PRODUCT_LOCK_CONFLICT);
        }
    }

    /**
     * 진행중 공구 기준으로 할인/단계/단가 계산.
     * 없으면 할인 0, 레벨 0, 단가는 원가.
     */
    private PriceSnapshot calcPriceSnapshot(Product product) {
        GroupBuy gb = groupBuyRepository
                .findTopByProductAndStatusOrderByCreatedAtDesc(product, GroupBuyStatus.OPEN)
                .orElse(null);

        if (gb == null) {
            double original = product.getOriginalPricePerBaseUnit();
            return new PriceSnapshot(0, 0.0, Math.round(original));
        }

        double totalQty = gb.getCurrentQty();
        GroupBuyStage cur = groupBuyStageRepository
                .findTopByGroupBuyAndStartQtyLessThanEqualOrderByStartQtyDesc(gb, totalQty);

        if (cur == null) {
            double original = product.getOriginalPricePerBaseUnit();
            return new PriceSnapshot(0, 0.0, Math.round(original));
        }

        // 레벨(1..N) 계산: startQty 오름차순으로 몇 번째인지
        int level = resolveLevelIndex(gb, cur);

        double discount = cur.getDiscountPercent();
        double applied = product.getOriginalPricePerBaseUnit() * ((100.0 - discount) / 100.0);
        long unitPrice = Math.round(applied);

        return new PriceSnapshot(level, discount, unitPrice);
    }

    private int resolveLevelIndex(GroupBuy gb, GroupBuyStage current) {
        List<GroupBuyStage> stages = groupBuyStageRepository.findByGroupBuyOrderByStartQtyAsc(gb);
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getId().equals(current.getId())) {
                return i + 1; // 1-based
            }
        }
        return 0;
    }

    private record PriceSnapshot(int level, double discountPercent, long unitPrice) {}
}