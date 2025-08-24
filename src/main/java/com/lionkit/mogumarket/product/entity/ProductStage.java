//package com.lionkit.mogumarket.product.entity;
//
//
//import com.lionkit.mogumarket.global.base.domain.BaseEntity;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.DecimalMin;
//import jakarta.validation.constraints.Max;
//import jakarta.validation.constraints.Min;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Table(
//
//        /**
//         *  유니크 제약: 한 상품에 대해 같은 단계번호(stage)나 시작값(start_base_qty)을 중복 등록하지 않도록 보장
//         * uk_stage_product_level: 같은 상품에서 동일 단계 번호 중복 방지
//         * uk_stage_product_start: 같은 상품에서 동일 시작값 중복 방지
//        */
//        uniqueConstraints = {
//                @UniqueConstraint(name="uk_stage_product_level", columnNames={"product_id","level"}),
//                @UniqueConstraint(name = "uk_stage_product_start", columnNames = {"product_id","start_base_qty"})
//        }
//)
//
//public class ProductStage extends BaseEntity {
//
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "product_stage_id")
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
//    private Product product;
//
//
//    /** 단계 시작값 (기준단위, g/ml/ea). */
//    @DecimalMin(value = "0.0", inclusive = true)
//    @Column(name = "start_base_qty", nullable = false)
//    private double startBaseQty;
//
//
//    /** 할인율(%) */
//    @Min(0) @Max(100)
//    @Column(nullable = false)
//    private double discountPercent;
//
//    /** 몇 단계인지 (1,2,3…) */
//    @Min(1)
//    @Column(nullable = false)
//    private int level;
//
//}
