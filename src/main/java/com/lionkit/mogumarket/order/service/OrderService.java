package com.lionkit.mogumarket.order.service;

import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.order.entity.OrderLine;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.entity.ProductStage;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.product.service.ProductStageService;
import com.lionkit.mogumarket.order.entity.Orders;
import com.lionkit.mogumarket.order.enums.OrderStatus;
import com.lionkit.mogumarket.order.repository.OrderRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class OrderService {

        private final ProductRepository productRepository;
        private final UserRepository userRepository;
        private final OrderRepository orderRepository;
        private final ProductStageService stageService;
        /**
         * Product 행을 비관적 락으로 선점
         * 재고/누적 수량 검증 & 반영
         * 현재 단계 조회 후 단가 계산
         * Orders(헤더) + OrderLine(라인) 생성/저장
         */

        public Long confirmSingleProductOrder(Long userId, Long productId, double qtyBase) {

            try {
                // TODO : productRepository 단을 직접 활용한 걸 productservice 단 메서드 정의하여 활용하도록 수정
                // 1) 비관적 락 적용
                Product product = productRepository
                        .findForUpdate(productId)
                        .orElseThrow(() -> new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));

                // 2) 재고 검증 & 수량 누적 반영
                product.increaseCurrentBaseQty(qtyBase);

                // 3) 현재 단계/단가 스냅샷
                ProductStage stage = stageService.getCurrentStage(product);
                if (stage == null) throw new BusinessException(ExceptionType.STAGE_NOT_DEFINED);

                double discountPercent = stage.getDiscountPercent();
                double unitPrice = stageService.getAppliedUnitPrice(product);

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
                        .orderedBaseQty(qtyBase)                     // 기준단위(g/ml/ea) 기준 수량
                        .levelSnapshot(stage.getLevel())
                        .discountPercentSnapshot(discountPercent)
                        .unitPriceSnapshot(unitPrice)                // 기준단위당 단가 스냅샷
                        .build();

                orders.getLines().add(line); // cascade = ALL 로 라인 자동 저장


                return orderRepository.save(orders).getId();

            }catch ( LockTimeoutException e) { // 락 대기초과(5초) — 사용자 재시도 유도
                    throw new BusinessException(ExceptionType.PRODUCT_LOCK_TIMEOUT);
                } catch ( PessimisticLockException e) { // 비관적 락 관련 실패
                    throw new BusinessException(ExceptionType.PRODUCT_LOCK_CONFLICT);
                }

        }



}

