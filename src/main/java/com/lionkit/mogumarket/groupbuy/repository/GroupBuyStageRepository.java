package com.lionkit.mogumarket.groupbuy.repository;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupBuyStageRepository extends JpaRepository<GroupBuyStage, Long> {
    List<GroupBuyStage> findByGroupBuyOrderByStartQtyAsc(GroupBuy groupBuy);
    GroupBuyStage findTopByGroupBuyAndStartQtyLessThanEqualOrderByStartQtyDesc(GroupBuy groupBuy, double qty);
}