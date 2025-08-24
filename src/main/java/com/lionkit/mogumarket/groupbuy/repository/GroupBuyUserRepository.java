package com.lionkit.mogumarket.groupbuy.repository;

import com.lionkit.mogumarket.groupbuy.domain.GroupBuyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupBuyUserRepository extends JpaRepository<GroupBuyUser, Long> {
    @Query("""
        select distinct gbu
        from GroupBuyUser gbu
        join fetch gbu.groupBuy gb
        join fetch gb.product p
        left join fetch gb.stages s
        where gbu.user.id = :userId
        """)
    List<GroupBuyUser> findAllWithGroupBuyAndStagesByUserId(@Param("userId") Long userId);
    @Query("select count(gbu) from GroupBuyUser gbu where gbu.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);


    Optional<GroupBuyUser> findByGroupBuyIdAndUserId(Long groupBuyId, Long userId);
    long countByGroupBuyId(Long groupBuyId); // 참가자 수 카운트용

}