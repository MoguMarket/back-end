package com.lionkit.mogumarket.point.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 현재 유저의 포인트 스냅샷에 대한 applicatoin 내부 사용 용도의 dto
 */
@Getter
@Setter
@Builder
public class PointSnapshotDto {
    Long userId;
    long balance;
    long hold;
    long available;
}
