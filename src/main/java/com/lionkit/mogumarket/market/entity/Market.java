package com.lionkit.mogumarket.market.entity;

import com.lionkit.mogumarket.global.base.domain.BaseEntity;
import com.lionkit.mogumarket.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Market extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "market_id")
    private Long id;

    private String marketCode;     // ex) a31370001

    private String name;           // ex) 설악눈메골시장

    private String landAddress;    // ex) 경기도 가평군 설악면 신천리 4-13~14

    private String roadAddress;    // ex) 경기도 가평군 설악면 신천중앙로 104-1

    private String sido;           // ex) 경기도

    private String sigungu;        // ex) 가평군

    private String description;    // 시장 설명

    @Column(columnDefinition = "DOUBLE")  // 예: 37.5665353
    private Double latitude;

    @Column(columnDefinition = "DOUBLE")  // 예: 126.9779692
    private Double longitude;

    // distance는 그대로 int 사용 (단위는 팀 컨벤션에 맞춰 m 또는 km 명시)
    private int distance;

    @OneToMany(mappedBy = "market", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Store> stores = new ArrayList<>();
}

