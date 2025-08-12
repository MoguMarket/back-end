package com.lionkit.mogumarket.product.entity;


import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        /**
         *  인덱스: 특정 상품(product_id)의 단계 시작값(start_base_qty)으로 빠르게 검색하기 위함
         * "이 상품의 현재 누적 수량이 7,500g일 때 적용할 단계 찾기" 같은 조회 성능 최적화
         *  product_id로 먼저 범위를 좁히고, start_base_qty로 정렬된 상태에서 빠르게 탐색 가능
        */
        indexes = {
                @Index(name = "idx_stage_product_start", columnList = "product_id,start_base_qty")
        },
        /**
         *  유니크 제약: 한 상품에 대해 같은 단계번호(stage)나 시작값(start_base_qty)을 중복 등록하지 않도록 보장
         * uk_stage_product_level: 같은 상품에서 동일 단계 번호 중복 방지
         * uk_stage_product_start: 같은 상품에서 동일 시작값 중복 방지
        */
        uniqueConstraints = {
                @UniqueConstraint(name="uk_stage_product_level", columnNames={"product_id","level"}),
                @UniqueConstraint(name = "uk_stage_product_start", columnNames = {"product_id","start_base_qty"})
        }
)

public class ProductStage extends BaseEntity {

    @Id @GeneratedValue @Column(name = "stage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    private Product product;


    /** 단계 시작값 (기준단위, g/ml/ea). */
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "start_base_qty", nullable = false)
    private double startBaseQty;


    /** 할인율(%) */
    @Min(0) @Max(100)
    @Column(nullable = false)
    private double discountPercent;

    /** 몇 단계인지 (1,2,3…) */
    @Min(1)
    @Column(nullable = false)
    private int level;

}
