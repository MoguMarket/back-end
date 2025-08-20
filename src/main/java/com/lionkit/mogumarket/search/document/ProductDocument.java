package com.lionkit.mogumarket.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDateTime;


@Document(indexName = "product_index")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setting(replicas = 0)
public class ProductDocument {

    /**
     * ES 의 @Id 는 jakarta.persistence.Id 가 아닌
     * org.springframework.data.annotation.Id 임을 주의
     */
    @Id
    private String id;


    /**
     * productId: DB 와 연동하기 위한 필수 필드
     */
    @Field(type = FieldType.Long)
    private Long productId;


    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;


    // 동기화 체크용
    @Field(type = FieldType.Date)
    private Instant updatedAt;



}