package com.lionkit.mogumarket.cart.repository;

import com.lionkit.mogumarket.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);
}