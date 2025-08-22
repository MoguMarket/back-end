package com.lionkit.mogumarket.point.dto.response;


import com.lionkit.mogumarket.point.entity.Point;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


/**
 * 현재 유저의 포인트 스냅샷에 대한 응답 dto
 */
@Getter
@Setter
@Builder
public class PointSnapshotResponseDto {
    Long userId;
    long balance;
    long hold;
    long available;

    public static PointSnapshotResponseDto fromEntity(Point point){
        return PointSnapshotResponseDto.builder()
                .userId(point.getUser().getId())
                .balance(point.getBalance())
                .hold(point.getHold())
                .available( point.getBalance() - point.getHold())
                .build();

    }

}
