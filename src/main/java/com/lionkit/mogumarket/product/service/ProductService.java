package com.lionkit.mogumarket.product.service;


import com.lionkit.mogumarket.product.dto.ProductSaveRequest;
import com.lionkit.mogumarket.product.dto.request.ProductPatchRequest;
import com.lionkit.mogumarket.product.dto.request.ProductUpdateRequest;
import com.lionkit.mogumarket.product.dto.response.ProductResponse;
import com.lionkit.mogumarket.product.entity.Product;
import com.lionkit.mogumarket.product.entity.ProductStage;
import com.lionkit.mogumarket.product.enums.SortType;
import com.lionkit.mogumarket.product.repository.ProductRepository;
import com.lionkit.mogumarket.product.repository.ProductStageRepository;
import com.lionkit.mogumarket.search.document.ProductDocument;
import com.lionkit.mogumarket.search.repository.ProductSearchRepository;
import com.lionkit.mogumarket.search.service.RedisSearchRankService;
import com.lionkit.mogumarket.store.entity.Store;
import com.lionkit.mogumarket.store.repsitory.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    private final ProductStageRepository stageRepository;
    private final ProductSearchRepository productSearchRepository;
    private final RedisSearchRankService redisSearchRankService;

    /* -----------------------
     * CREATE
     * ----------------------*/
    @Transactional
    public Long saveProduct(ProductSaveRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Store ID"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .unit(request.getUnit())
                .originalPricePerBaseUnit(request.getOriginalPrice())
                .stock(request.getStock())
                .currentBaseQty(0) // 새 상품은 판매 수량 0
                .deadline(request.getDeadline())
                .imageUrl(request.getImageUrl())
                .store(store)
                .build();

        Product saved = productRepository.save(product);

        // 검색 색인 (선택)
        if (productSearchRepository != null) {
            ProductDocument doc = ProductDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .name(saved.getName())
                    .description(saved.getDescription())
                    .build();
            productSearchRepository.save(doc);
        }

        // 검색 랭킹 (선택)
        if (redisSearchRankService != null) {
            redisSearchRankService.increaseKeywordScore(saved.getName());
        }

        return saved.getId();
    }

    /* -----------------------
     * READ - 단건
     * ----------------------*/
    @Transactional(readOnly = true)
    public ProductResponse getProductResponse(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return ProductResponse.fromEntity(product);
    }

    /* -----------------------
     * READ - 목록(페이징/정렬/스토어 필터)
     * ----------------------*/
    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Integer page, Integer size, Long storeId, SortType sort) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size < 1 || size > 100) ? 10 : size;

        // DB 정렬 가능한 경우는 Pageable로 한 번에
        Pageable pageableDb = switch (sort) {
            case NEWEST -> PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
            case DEADLINE -> PageRequest.of(p, s, Sort.by(Sort.Direction.ASC, "deadline"));
            case SALES -> PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "currentBaseQty"));
            default -> null; // RATING, DISCOUNT 는 메모리 정렬
        };

        // 1) storeId 필터링을 위해 기본 조회
        Page<Product> dbPage;
        if (pageableDb != null) {
            // JPA 쿼리 메서드가 없다면 전체 조회 후 메모리 필터링으로 처리 (규모 커지면 전용 repo 메서드 권장)
            dbPage = productRepository.findAll(pageableDb);
            if (storeId != null) {
                List<Product> filtered = dbPage.getContent().stream()
                        .filter(pv -> pv.getStore() != null && Objects.equals(pv.getStore().getId(), storeId))
                        .toList();
                // totalElements 정확히 하려면 전용 count 쿼리가 필요하지만, 간단화를 위해 content만 필터
                dbPage = new PageImpl<>(filtered, pageableDb, filtered.size());
            }
            // RATING, DISCOUNT 아닌 경우 여기서 바로 반환
            if (sort == SortType.NEWEST || sort == SortType.DEADLINE || sort == SortType.SALES) {
                return dbPage.map(ProductResponse::fromEntity);
            }
        } else {
            // DB 정렬이 불가능한 정렬의 경우, 우선 기본 정렬로 페이지 조각을 가져오고 난 뒤 메모리 정렬
            Pageable fallback = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
            dbPage = productRepository.findAll(fallback);
            if (storeId != null) {
                List<Product> filtered = dbPage.getContent().stream()
                        .filter(pv -> pv.getStore() != null && Objects.equals(pv.getStore().getId(), storeId))
                        .toList();
                dbPage = new PageImpl<>(filtered, fallback, filtered.size());
            }
        }

        // 2) 메모리 정렬이 필요한 케이스 (RATING, DISCOUNT)
        List<Product> content = new ArrayList<>(dbPage.getContent());

        Comparator<Product> cmp = switch (sort) {
            case RATING -> Comparator.comparingDouble(this::avgRating).reversed();
            case DISCOUNT -> Comparator.comparingDouble(this::currentDiscountPercent).reversed();
            default -> Comparator.comparing(Product::getId).reversed(); // fallback
        };

        content.sort(cmp);

        List<ProductResponse> responses = content.stream()
                .map(ProductResponse::fromEntity)
                .toList();

        return new PageImpl<>(responses, dbPage.getPageable(), dbPage.getTotalElements());
    }

    /* -----------------------
     * UPDATE - 전체 수정
     * ----------------------*/
    @Transactional
    public void updateProduct(Long id, ProductUpdateRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (req.getName() != null) setField(product, "name", req.getName());
        if (req.getDescription() != null) setField(product, "description", req.getDescription());
        if (req.getUnit() != null) setField(product, "unit", req.getUnit());
        if (req.getOriginalPrice() != null) setField(product, "originalPricePerBaseUnit", req.getOriginalPrice());
        if (req.getStock() != null) setField(product, "stock", req.getStock());
        if (req.getImageUrl() != null) setField(product, "imageUrl", req.getImageUrl());
        if (req.getDeadline() != null) setField(product, "deadline", req.getDeadline());

        if (req.getStoreId() != null) {
            Store store = storeRepository.findById(req.getStoreId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Store ID"));
            setField(product, "store", store);
        }

        productRepository.save(product);
    }

    /* -----------------------
     * PATCH - 부분 수정
     * ----------------------*/
    @Transactional
    public void patchProduct(Long id, ProductPatchRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (req.getOriginalPrice() != null) setField(product, "originalPricePerBaseUnit", req.getOriginalPrice());
        if (req.getImageUrl() != null) setField(product, "imageUrl", req.getImageUrl());
        if (req.getDeadline() != null) setField(product, "deadline", req.getDeadline());

        productRepository.save(product);
    }

    /* -----------------------
     * DELETE
     * ----------------------*/
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        productRepository.delete(product);
    }

    /* ==========================================================
     * 내부 유틸
     * ========================================================== */

    /** 현재 단계 할인율(%) — 없으면 0 */
    private double currentDiscountPercent(Product p) {
        if (p == null) return 0.0;
        double qty = p.getCurrentBaseQty();
        ProductStage cur = stageRepository
                .findTopByProductAndStartBaseQtyLessThanEqualOrderByStartBaseQtyDesc(p, qty);
        return (cur == null) ? 0.0 : cur.getDiscountPercent();
    }

    /** 평균 별점 — 없으면 0 */
    private double avgRating(Product p) {
        if (p == null || p.getReviews() == null || p.getReviews().isEmpty()) return 0.0;
        return p.getReviews().stream()
                .map(r -> r.getRating() == null ? 0 : r.getRating())
                .mapToInt(Integer::intValue)
                .average().orElse(0.0);
    }

    /** 엔티티 필드 직접 세팅 (엔티티에 setter 없을 때 임시 사용) */
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (NoSuchFieldException e) {
            // 상위 클래스(BaseEntity 등)에 있을 수 있음
            try {
                Field f = target.getClass().getSuperclass().getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
            } catch (Exception ex) {
                throw new IllegalStateException("필드 설정 실패: " + fieldName, ex);
            }
        } catch (Exception e) {
            throw new IllegalStateException("필드 설정 실패: " + fieldName, e);
        }
    }
}