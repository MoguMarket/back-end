package com.lionkit.mogumarket.order.service;

import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import com.lionkit.mogumarket.groupbuy.enums.GroupBuyStatus;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyStageRepository;
import com.lionkit.mogumarket.order.entity.OrderLine;
import com.lionkit.mogumarket.order.entity.Orders;
import com.lionkit.mogumarket.order.enums.OrderStatus;
import com.lionkit.mogumarket.order.repository.OrderLineRepository;
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

import java.util.List;

// TODO : payment 연결
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;


    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyStageRepository groupBuyStageRepository;

    /**
     * 공구 주문 경로
     */
    private Long placeGroupBuyOrder(Orders orders, GroupBuy gb, Product product, double qtyBase) {

        // 2) GroupBuy 잠금 (항상 Product → GroupBuy lock 순서 유지)
        GroupBuy gbLocked = groupBuyRepository.findByIdForUpdate(gb.getId())
                .orElseThrow(() -> new BusinessException(ExceptionType.GROUP_BUY_NOT_FOUND));

        if (gbLocked.getStatus() != GroupBuyStatus.OPEN) {
            throw new BusinessException(ExceptionType.GROUP_BUY_NOT_OPEN);
        }

            // 남은 전체 재고 = product.stock - product.currentBaseQty (currentBaseQty에는 일반+공구 누적이 이미 포함됨)
        double remainStock = product.getStock() - product.getCurrentBaseQty();
        if (qtyBase > remainStock) throw new BusinessException(ExceptionType.STOCK_OVERFLOW);


        // 남은 공구 한도 = targetQty - gb.currentQty
        double remainGroup  =  gbLocked.getTargetQty() - gbLocked.getCurrentQty();
        if (qtyBase > remainGroup ) throw new BusinessException(ExceptionType.STOCK_OVERFLOW);

        // 현재 단계(참여 시점) 조회
        double totalQtyBeforeJoin = gbLocked.getCurrentQty();
        GroupBuyStage curGroupBuyStage = groupBuyStageRepository
                .findTopByGroupBuyAndStartQtyLessThanEqualOrderByStartQtyDesc(gbLocked, totalQtyBeforeJoin)
                .orElseThrow(()->new BusinessException(ExceptionType.GROUP_BUY_STAGE_NOT_FOUND));


        // 스냅샷 기반
        int level = curGroupBuyStage.getLevel();
        double discount = curGroupBuyStage.getDiscountPercent();
        double appliedUnitPrice = curGroupBuyStage.getAppliedUnitPrice();

        /**
         * 누적 수량 반영
         */
        product.increaseCurrentBaseQty(qtyBase); // 전체 누적(일반+공구)에 포함
        gbLocked.increaseQty(qtyBase); // 공구 누적 수량 반영

        OrderLine line = OrderLine.builder()
                .orders(orders)
                .product(product)
                .groupBuy(gbLocked)
                .orderedBaseQty(qtyBase)
                .levelSnapshot(level)
                .discountPercentSnapshot(discount)
                .unitPriceSnapshot(appliedUnitPrice)
                .build();

        orders.addLine(line); // 양방향 연결
        return orderRepository.save(orders).getId(); // 캐스케이드(cascade=PERSIST )에 의해 자동 저장된다
    }

    /** 일반 주문 경로 (공구 미참여) */
    private Long placeNormalOrder(Orders orders, Product product, GroupBuy openGb, double qtyBase) {
        // 남은 전체 재고 = stock - product.currentBaseQty
        double remainStock = product.getStock() - product.getCurrentBaseQty();
        if (qtyBase > remainStock) throw new BusinessException(ExceptionType.STOCK_OVERFLOW);

        // 전체 누적 증가
        product.increaseCurrentBaseQty(qtyBase);

        double unitPrice = Math.round(product.getOriginalPricePerBaseUnit());

        // 공구 미참여시 groupbuy 는 null
        OrderLine line = OrderLine.builder()
                .orders(orders)
                .product(product)
                .orderedBaseQty(qtyBase)
                .levelSnapshot(0)
                .discountPercentSnapshot(0.0)
                .unitPriceSnapshot(unitPrice)
                .build();

        orders.addLine(line); // 양방향 연결
        return orderRepository.save(orders).getId();
    }




    /**
     * 단일 상품 주문 확정
     * @param participateInGroupBuy true=공구참여, false=미참여(즉시구매)
     */
    public Long confirmSingleProductOrder(Long userId, Long productId, double qtyBase, boolean participateInGroupBuy){
        if (qtyBase <= 0) throw new BusinessException(ExceptionType.INVALID_QTYBASE);

        try {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

            // 1) 상품 비관적 락 ( 항상 Product 먼저 잠금 )
            Product product = productRepository.findForUpdate(productId)
                    .orElseThrow(() -> new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));

            // 공구는 상품 1:1 이므로 필요시 조회
            GroupBuy openGb = groupBuyRepository
                    .findTopByProductAndStatusOrderByCreatedAtDesc(product, GroupBuyStatus.OPEN)
                    .orElse(null);


            Orders orders = Orders.builder()
                    .user(user)
                    .status(OrderStatus.CONFIRMED)
                    .build();

            // 공구에 참여하기 == true 인경우
            if (participateInGroupBuy) {
                if (openGb == null) throw new BusinessException(ExceptionType.GROUP_BUY_NOT_FOUND);
                else return placeGroupBuyOrder(orders, openGb, product, qtyBase);
            }

            // 공구에 참여하지 않고 개인 구매를 하는 경우
            else return placeNormalOrder(orders, product, openGb, qtyBase);



        } catch (LockTimeoutException e) {
            throw new BusinessException(ExceptionType.PRODUCT_LOCK_TIMEOUT);
        } catch (PessimisticLockException e) {
            throw new BusinessException(ExceptionType.PRODUCT_LOCK_CONFLICT);
        }
    }

    /**
     * 공구 종료 시 OrderLine의 final 스냅샷 채우기
     * - GroupBuyService.closeGroupBuy(...) 직후 호출 권장
     */
    public void finalizeSnapshotsForGroupBuy(Long groupBuyId) {
        GroupBuy gb = groupBuyRepository.findByIdForUpdate(groupBuyId)
                .orElseThrow(() -> new BusinessException(ExceptionType.GROUP_BUY_NOT_FOUND));

        double totalGbQty = gb.getCurrentQty(); // 공구 누적 기준
        GroupBuyStage finalStage = groupBuyStageRepository
                .findTopByGroupBuyAndStartQtyLessThanEqualOrderByStartQtyDesc(gb, totalGbQty)
                .orElseThrow(() -> new BusinessException(ExceptionType.GROUP_BUY_STAGE_NOT_FOUND));

        int finalLevel = finalStage.getLevel();
        double finalDiscount = finalStage.getDiscountPercent();
        double finalUnitPrice = finalStage.getAppliedUnitPrice();


        /**
         * lines 가 없다 == 해당 공구가 종료되었지만 실제로 참여한 인원이 없다.
         * 안타깝긴해도 발생할 수 있는 상황이니 EXCEPTION 을 발생시키진 않습니다. (null 이면 조용히 넘어감)
         */
        List<OrderLine> lines = orderLineRepository.findByGroupBuy(gb);
        for (OrderLine line : lines) {
            line.finalizeSnapshots(finalLevel, finalDiscount, finalUnitPrice);
        }
        // @Transactional -> Dirty Checking 반영됩니다.
    }

    /**
     * 결재 실패 시 rollback
     * @param ordersId
     */
    @SuppressWarnings("unused")
    public void rollbackStocks(Long ordersId) {
        Orders orders = orderRepository.findById(ordersId)
                .orElseThrow(() -> new BusinessException(ExceptionType.ORDER_NOT_FOUND));

        // 이미 실패로 처리된 주문이면 멱등 처리
        if (orders.getStatus() == OrderStatus.FAILED) return;

        // 라인별로 제품/공구 되돌리기(락은 라인 단위로 보수적으로)
        for (OrderLine line : orders.getLines()) {
            // Product lock & rollback
            Product pLocked = productRepository.findForUpdate(line.getProduct().getId())
                    .orElseThrow(() -> new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));
            pLocked.decreaseCurrentBaseQty(line.getOrderedBaseQty());

            // GroupBuy lock & rollback (참여 라인만)
            GroupBuy gb = line.getGroupBuy();
            if (gb != null) {
                GroupBuy gbLocked = groupBuyRepository.findByIdForUpdate(gb.getId())
                        .orElseThrow(() -> new BusinessException(ExceptionType.GROUP_BUY_NOT_FOUND));
                gbLocked.decreaseQty(line.getOrderedBaseQty());
            }
        }

        orders.updateStatus(OrderStatus.FAILED);
    }




}