package com.lionkit.mogumarket.search.document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;


@Document(indexName = "product_index")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setting(replicas = 0)
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long productId;

    @Field(type = FieldType.Long)
    private Long storeId;

    @Field(type = FieldType.Long)
    private Long marketId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;

    @Field(type = FieldType.Long)
    private Long updatedAtMillis;

}