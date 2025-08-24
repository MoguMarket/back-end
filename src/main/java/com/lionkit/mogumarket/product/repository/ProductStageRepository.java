//package com.lionkit.mogumarket.product.repository;
//
//
//import com.lionkit.mogumarket.product.entity.Product;
//import com.lionkit.mogumarket.product.entity.ProductStage;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//public interface ProductStageRepository extends JpaRepository<ProductStage, Long> {
//
//    /** 현재 단계를 조회 :
//     * start ≤ qty 중 가장 큰 start
//     */
//    ProductStage findTopByProductAndStartBaseQtyLessThanEqualOrderByStartBaseQtyDesc(
//            Product product, double qty
//    );
//
//    /** 다음 단계: start > qty 중 start가 가장 작은 행 */
//    ProductStage findTopByProductAndStartBaseQtyGreaterThanOrderByStartBaseQtyAsc(
//            Product product, double qty
//    );
//
//}
