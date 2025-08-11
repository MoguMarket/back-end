package com.lionkit.mogumarket.product.repository;

import com.lionkit.mogumarket.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product>findByModifiedAtAfter(LocalDateTime lastSyncTime);
}