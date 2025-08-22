package com.lionkit.mogumarket.groupbuy.scheduler;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStatus;
import com.lionkit.mogumarket.groupbuy.repository.GroupBuyRepository;
import com.lionkit.mogumarket.groupbuy.service.GroupBuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyScheduler {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyService groupBuyService;

    /**
     * 매 1분마다 마감 대상 자동 처리
     * (운영 시 크론/주기 조정 가능)
     */
    @Scheduled(cron = "0 * * * * *")
    public void closeExpiredGroupBuys() {
        LocalDateTime now = LocalDateTime.now();
        List<GroupBuy> targets = groupBuyRepository
                .findByStatusAndEndAtBefore(GroupBuyStatus.OPEN, now);

        for (GroupBuy gb : targets) {
            try {
                groupBuyService.closeGroupBuy(gb.getId());
                log.info("[GroupBuyScheduler] closed groupBuy id={}", gb.getId());
            } catch (Exception e) {
                log.warn("[GroupBuyScheduler] close failed. id={}, reason={}", gb.getId(), e.getMessage());
            }
        }
    }
}