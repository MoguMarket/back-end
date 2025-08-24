// com.lionkit.mogumarket.groupbuy.service.GroupBuyService
package com.lionkit.mogumarket.groupbuy.service;

import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.groupbuy.domain.*;
import com.lionkit.mogumarket.groupbuy.dto.response.GroupBuyStatusResponse;
import com.lionkit.mogumarket.groupbuy.enums.GroupBuyStatus;
import com.lionkit.mogumarket.groupbuy.repository.*;
import com.lionkit.mogumarket.notification.service.NotificationFacade; // ⬅️ 파사드 사용
import com.lionkit.mogumarket.order.repository.OrderLineRepository;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupBuyService {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyStageRepository stageRepository;
    private final ProductRepository productRepository;
    private final OrderLineRepository orderLineRepository;
    private final UserRepository userRepository;

    // FCMService 대신 파사드
    private final NotificationFacade notificationFacade;

    /** 공동구매 생성 */
    @Transactional
    public Long createGroupBuy(Long productId, double targetQty,
                               double maxDiscountPercent, int stageCount) {
        if (targetQty <= 0) throw new IllegalArgumentException("targetQty must be > 0");
        if (maxDiscountPercent < 0 || maxDiscountPercent >= 100) throw new IllegalArgumentException("maxDiscount out of range");
        if (stageCount < 1 || stageCount > 10) throw new IllegalArgumentException("stageCount out of range");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ExceptionType.PRODUCT_NOT_FOUND));

        GroupBuy groupBuy = GroupBuy.builder()
                .product(product)
                .targetQty(targetQty)
                .currentQty(0)
                .status(GroupBuyStatus.OPEN)
                .maxDiscountPercent(maxDiscountPercent)
                .basePricePerBaseUnitSnapshot(product.getOriginalPricePerBaseUnit()) // 스냅샷 동결
                .build();

        // 단계 자동 생성 (균등 분할 예시)
        for (int level = 1; level <= stageCount; level++) {
            double startQty = Math.round((targetQty * level) / (stageCount + 1)); // 목표까지 균등히 배치
            double discount = Math.round((maxDiscountPercent * level) / stageCount * 10) / 10.0; // 소수1자리

            GroupBuyStage groupBuyStage = GroupBuyStage.builder()
                    .groupBuy(groupBuy)
                    .level(level)
                    .startQty(startQty)
                    .discountPercent(discount)
                    .build();

            groupBuyStage.computeAppliedPriceFromSnapshot(); // 확정 단가 기록
            groupBuy.getStages().add(groupBuyStage);
        }

        return groupBuyRepository.save(groupBuy).getId();
    }
    /** 공동구매 참여 */
    @Transactional
    public void participate(Long groupBuyId, Long userId, double qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");

        GroupBuy gb = groupBuyRepository.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("공동구매 없음"));
        if (gb.getStatus() != GroupBuyStatus.OPEN) {
            throw new IllegalStateException("이미 마감/종료된 공동구매입니다.");
        }


        // 재고 체크(상품 총재고 - 현재 누적)
        Product p = gb.getProduct();
        double remain = p.getStock() - gb.getCurrentQty();
        if (qty > remain) throw new IllegalStateException("재고 부족: 잔여 " + remain);

        // 수량 반영 및 주문 기록
        gb.increaseQty(qty);

        // TODO : 여기서 실제 주문(Orders/OrderLine) 생성 : OrderService에서 처리하도록 위임

        // 참여자 본인에게 알림
        notifyUserSafe(
                userId,
                "공동구매 참여 완료",
                "[" + p.getName() + "]에 " + qty + p.getUnit().getTitle() + " 참여 완료!",
                Map.of("event","GROUPBUY_JOINED","groupBuyId", String.valueOf(gb.getId()))
        );

        // 목표 달성 시 전체 브로드캐스트
        if (gb.getStatus() == GroupBuyStatus.SUCCESS) {
            notifyAllParticipants(
                    gb,
                    "목표 달성! 결제/수령 안내",
                    "[" + p.getName() + "] 공동구매가 목표를 달성했습니다. 상세 안내를 확인해주세요.",
                    Map.of("event","GROUPBUY_SUCCESS","groupBuyId", String.valueOf(gb.getId()))
            );
        }
    }

    /** 공동구매 마감 */
    @Transactional
    public void closeGroupBuy(Long groupBuyId) {
        GroupBuy gb = groupBuyRepository.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("공동구매 없음"));
        gb.close();

        notifyAllParticipants(
                gb,
                "공동구매 마감",
                "[" + gb.getProduct().getName() + "] 공동구매가 마감되었습니다.",
                Map.of("event","GROUPBUY_CLOSED","groupBuyId", String.valueOf(gb.getId()))
        );
    }

    /** 공동구매 상태 + 단계별 할인 + 단가 계산 */
    @Transactional(readOnly = true)
    public GroupBuyStatusResponse getGroupBuyStatus(Long groupBuyId) {
        GroupBuy gb = groupBuyRepository.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("공동구매 없음"));

        double totalQty = gb.getCurrentQty();


        GroupBuyStage cur = stageRepository
                .findTopByGroupBuyAndStartQtyLessThanEqualOrderByStartQtyDesc(gb, totalQty)
                .orElse(null);

        GroupBuyStage next = stageRepository
                .findTopByGroupBuyAndStartQtyGreaterThanOrderByStartQtyAsc(gb, totalQty)
                .orElse(null);

        double currentDiscount = (cur != null) ? cur.getDiscountPercent() : 0.0;
        long appliedUnitPrice = (cur != null) ? cur.getAppliedUnitPrice()
                : Math.round(gb.getBasePricePerBaseUnitSnapshot());

        double remainingToNext = (next != null) ? (next.getStartQty() - totalQty) : 0.0;

        return GroupBuyStatusResponse.builder()
                .groupBuyId(gb.getId())
                .status(gb.getStatus())
                .totalQuantity(totalQty)
                .targetQuantity(gb.getTargetQty())
                .currentDiscount(currentDiscount)
                .remainingToNextStage(remainingToNext)
                .originalUnitPrice(gb.getBasePricePerBaseUnitSnapshot())
                .appliedUnitPrice(appliedUnitPrice)
                .build();
    }

    /* ───────────── 내부 알림 유틸 ───────────── */

    private void notifyUserSafe(Long userId, String title, String body, Map<String,String> data) {
        try {
            notificationFacade.notifyUsers(List.of(userId), title, body, data); // 단일 유저도 List로 감싸기
        } catch (Exception ignored) {
            // 로깅만 하고 진행
        }
    }

    private void notifyAllParticipants(GroupBuy gb, String title, String body, Map<String,String> data) {
        List<Long> userIds = orderLineRepository.findParticipantUserIds(gb);
        if (userIds.isEmpty()) return;

        try {
            notificationFacade.notifyUsers(userIds, title, body, data);
        } catch (Exception ignored) {
            // 일부 실패는 파사드/FCM 레이어에서 정리
            // 실패는 로깅만 (서비스 흐름 저해 X)

        }
    }
}