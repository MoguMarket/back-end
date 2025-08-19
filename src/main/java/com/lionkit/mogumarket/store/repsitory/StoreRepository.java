package com.lionkit.mogumarket.store.repsitory;

import com.lionkit.mogumarket.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}