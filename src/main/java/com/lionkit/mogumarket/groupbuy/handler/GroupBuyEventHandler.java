package com.lionkit.mogumarket.groupbuy.handler;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.dto.GroupBuyClosedEvent;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository;
import com.lionkit.mogumarket.notification.service.NotificationFacade;
import com.lionkit.mogumarket.order.repository.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GroupBuyEventHandler {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderLineRepository orderLineRepository;
    private final NotificationFacade notificationFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onClosed(GroupBuyClosedEvent event) {
        GroupBuy gb = groupBuyRepository.findById(event.groupBuyId())
                .orElse(null);
        if (gb == null) return;

        var userIds = orderLineRepository.findParticipantUserIds(gb);
        if (userIds.isEmpty()) return;

        // 여기서 예외가 나도 본 거래에는 영향 없음
        notificationFacade.notifyUsers(
                userIds,
                "공동구매 마감",
                "[" + gb.getProduct().getName() + "] 공동구매가 마감되었습니다.",
                Map.of("event","GROUPBUY_CLOSED","groupBuyId", String.valueOf(gb.getId()))
        );
    }
}
