package com.lionkit.mogumarket.search.document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.lang.annotation.Documented;

@Document(indexName = "product_index")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setting(settingPath = "/elasticsearch/settings.json")
@Mapping(mappingPath = "/elasticsearch/mapping.json")
public class ProductDocument {
    @Id
    private String id;
    private String name;
    private String description;
}