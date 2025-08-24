package com.lionkit.mogumarket.groupbuy.repository;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.groupbuy.domain.GroupBuyStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupBuyStageRepository extends JpaRepository<GroupBuyStage, Long> {
    List<GroupBuyStage> findByGroupBuyOrderByStartQtyAsc(GroupBuy groupBuy);
    Optional<GroupBuyStage> findTopByGroupBuyAndStartQtyLessThanEqualOrderByStartQtyDesc(GroupBuy groupBuy, double qty);
    /** 다음 단계: startQty > qty 중 가장 작은 start */
    Optional<GroupBuyStage> findTopByGroupBuyAndStartQtyGreaterThanOrderByStartQtyAsc(GroupBuy gb, double qty);


}