package com.lionkit.mogumarket.groupbuy.service;

import com.lionkit.mogumarket.groupbuy.domain.*;
import com.lionkit.mogumarket.groupbuy.dto.response.GroupBuyStatusResponse;
import com.lionkit.mogumarket.groupbuy.repository.*;
import com.lionkit.mogumarket.notification.service.NotificationFacade;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupBuyService {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyOrderRepository orderRepository;
    private final GroupBuyStageRepository stageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationFacade notificationFacade;

    @Transactional
    public Long createGroupBuy(Long productId, Long userId, double targetQty,
                               double maxDiscountPercent, int stageCount, LocalDateTime startAt, LocalDateTime endAt) {
        if (targetQty <= 0) throw new IllegalArgumentException("targetQty must be > 0");
        if (maxDiscountPercent < 0 || maxDiscountPercent > 90) throw new IllegalArgumentException("maxDiscount out of range");
        if (stageCount < 1 || stageCount > 10) throw new IllegalArgumentException("stageCount out of range");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        GroupBuy groupBuy = GroupBuy.builder()
                .product(product)
                .createdBy(creator)
                .targetQty(targetQty)
                .currentQty(0)
                .status(GroupBuyStatus.OPEN)
                .maxDiscountPercent(maxDiscountPercent)
                .startAt(startAt)
                .endAt(endAt)
                .build();

        for (int i = 1; i <= stageCount; i++) {
            double startQty = Math.round((targetQty * i) / (stageCount + 1));
            double discount = Math.round((maxDiscountPercent * i) / stageCount * 10) / 10.0;
            groupBuy.getStages().add(GroupBuyStage.builder()
                    .groupBuy(groupBuy)
                    .startQty(startQty)
                    .discountPercent(discount)
                    .build());
        }

        return groupBuyRepository.save(groupBuy).getId();
    }

    @Transactional
    public void participate(Long groupBuyId, Long userId, double qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");

        GroupBuy gb = groupBuyRepository.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("공동구매 없음"));
        if (gb.getStatus() != GroupBuyStatus.OPEN) {
            throw new IllegalStateException("이미 마감/종료된 공동구매입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Product p = gb.getProduct();
        double remain = p.getStock() - gb.getCurrentQty();
        if (qty > remain) throw new IllegalStateException("재고 부족: 잔여 " + remain);

        gb.increaseQty(qty);
        GroupBuyOrder order = GroupBuyOrder.builder()
                .groupBuy(gb)
                .user(user)
                .quantity(qty)
                .build();
        orderRepository.save(order);

        notifyUserSafe(
                user.getId(),
                "공동구매 참여 완료",
                "[" + p.getName() + "]에 " + qty + p.getUnit().getTitle() + " 참여 완료!",
                Map.of("event","GROUPBUY_JOINED","groupBuyId", String.valueOf(gb.getId()))
        );

        if (gb.getStatus() == GroupBuyStatus.SUCCESS) {
            notifyAllParticipants(
                    gb,
                    "목표 달성! 결제/수령 안내",
                    "[" + p.getName() + "] 공동구매가 목표를 달성했습니다. 상세 안내를 확인해주세요.",
                    Map.of("event","GROUPBUY_SUCCESS","groupBuyId", String.valueOf(gb.getId()))
            );
        }
    }

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

    @Transactional(readOnly = true)
    public GroupBuyStatusResponse getGroupBuyStatus(Long groupBuyId) {
        GroupBuy gb = groupBuyRepository.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("공동구매 없음"));

        double totalQty = orderRepository.findByGroupBuy(gb).stream()
                .mapToDouble(GroupBuyOrder::getQuantity)
                .sum();

        var stages = stageRepository.findByGroupBuyOrderByStartQtyAsc(gb);
        int stageCount = stages.size();

        double currentDiscount = stages.stream()
                .filter(s -> totalQty >= s.getStartQty())
                .max(Comparator.comparingDouble(GroupBuyStage::getStartQty))
                .map(GroupBuyStage::getDiscountPercent)
                .orElse(0.0);

        double remainingToNext = stages.stream()
                .filter(s -> totalQty < s.getStartQty())
                .min(Comparator.comparingDouble(GroupBuyStage::getStartQty))
                .map(s -> s.getStartQty() - totalQty)
                .orElse(0.0);

        double original = gb.getProduct().getOriginalPricePerBaseUnit();
        long applied = Math.round(original * ((100.0 - currentDiscount) / 100.0));

        return GroupBuyStatusResponse.builder()
                .groupBuyId(gb.getId())
                .status(gb.getStatus())
                .totalQuantity(totalQty)
                .targetQuantity(gb.getTargetQty())
                .currentDiscount(currentDiscount)
                .remainingToNextStage(remainingToNext)
                .originalUnitPrice(original)
                .appliedUnitPrice(applied)
                .stageCount(stageCount)
                .build();
    }

    private void notifyUserSafe(Long userId, String title, String body, Map<String,String> data) {
        try {
            notificationFacade.notifyUsers(List.of(userId), title, body, data);
        } catch (Exception ignored) {}
    }

    private void notifyAllParticipants(GroupBuy gb, String title, String body, Map<String,String> data) {
        List<Long> userIds = orderRepository.findByGroupBuy(gb).stream()
                .map(o -> o.getUser().getId())
                .distinct()
                .toList();
        try {
            notificationFacade.notifyUsers(userIds, title, body, data);
        } catch (Exception ignored) {}
    }
}