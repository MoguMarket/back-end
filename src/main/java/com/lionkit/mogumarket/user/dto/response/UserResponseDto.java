package com.lionkit.mogumarket.user.dto.response;

import com.lionkit.mogumarket.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//TODO : 해당 유저가 참여중인 공구 정보도 추가

@Setter
@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String username;
    private String nickname;
    private String email;

    /**
     * 유저의 회원가입과 함께 생성된
     * pointwallet 의 id 를 반환합니다.
     */
    private Long pointWalletId;

    /**
     * 유저의 회원가입과 함께 생성된
     * cart 의 id 를 반환합니다.
     */
    private Long cartId;

    public static UserResponseDto fromEntity(User user, Long pointWalletId , Long cartId) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .pointWalletId(pointWalletId)
                .cartId(cartId)
                .build();
    }
}
