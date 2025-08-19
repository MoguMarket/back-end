package com.lionkit.mogumarket.global.base.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseEntity {

    // INSERT 시 자동 세팅, 이후 수정 불가
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;

    // INSERT 시 현재시간, UPDATE 시 자동 갱신
    @UpdateTimestamp
    @Column(name = "modified_at", nullable = false,
            columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    private LocalDateTime modifiedAt;
}