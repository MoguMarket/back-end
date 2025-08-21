package com.lionkit.mogumarket.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
@AllArgsConstructor
public enum SortType {
    DISCOUNT("discount", Sort.Direction.DESC),     // 할인율순
    DEADLINE("deadline", Sort.Direction.ASC),      // 종료 임박순
    SALES("sales", Sort.Direction.DESC),           // 판매순
    RATING("rating", Sort.Direction.DESC),         // 별점순
    NEWEST("createdAt", Sort.Direction.DESC);      // 신상품순

    private final String field;        // 매핑할 DB 컬럼명 (Entity 필드명)
    private final Sort.Direction direction; // 정렬 방향
}