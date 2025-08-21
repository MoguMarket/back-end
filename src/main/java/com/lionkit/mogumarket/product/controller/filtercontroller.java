package com.lionkit.mogumarket.product.controller;

import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.enums.SortType;
import com.lionkit.mogumarket.product.service.FilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class filtercontroller {

    private final FilterService filterservice;  // 대문자 S

    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "NEWEST") SortType sort
    ) {
        return ResponseEntity.ok(filterservice.getProducts(sort));
    }
}
