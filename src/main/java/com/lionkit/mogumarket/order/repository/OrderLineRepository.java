package com.lionkit.mogumarket.order.repository;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuy;
import com.lionkit.mogumarket.order.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    @Query("""
           select ol
           from OrderLine ol
           join fetch ol.orders o
           where ol.id = :orderLineId
           """)
    Optional<OrderLine> findByIdWithOrders(Long orderLineId);


    @Query("select distinct l.orders.user.id from OrderLine l where l.groupBuy = :gb")
    List<Long> findParticipantUserIds(@Param("gb") GroupBuy gb);

    @Query("select coalesce(sum(l.orderedBaseQty), 0) from OrderLine l where l.groupBuy = :gb")
    Double sumOrderedQtyByGroupBuy(@Param("gb") GroupBuy gb);

    List<OrderLine> findByGroupBuy(GroupBuy gb); // 공구 종료 시 final*Snapshot 채우는 데 사용

}