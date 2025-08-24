package com.lionkit.mogumarket.cart.repository;

import com.lionkit.mogumarket.cart.entity.CartLine;
import com.lionkit.mogumarket.cart.enums.PurchaseRoute;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartLineRepository extends JpaRepository<CartLine, Long> {

    @EntityGraph(attributePaths = {"product", "product.groupBuy"})
    List<CartLine> findAllWithProductAndGroupBuyByCartUserId(@Param("userId") Long userId);


    List<CartLine> findAllByCart_User_Id(Long userId);

    Optional<CartLine> findByCart_User_IdAndProduct_IdAndRoute(Long userId, Long productId, PurchaseRoute route);

    void deleteByCart_User_IdAndProduct_IdAndRoute(Long userId, Long productId, PurchaseRoute route);

    void deleteAllByCart_User_Id(Long userId);

}
