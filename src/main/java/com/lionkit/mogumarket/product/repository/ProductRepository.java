package com.lionkit.mogumarket.product.repository;

import com.lionkit.mogumarket.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}