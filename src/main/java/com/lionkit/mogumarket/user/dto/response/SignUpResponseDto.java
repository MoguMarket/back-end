package com.lionkit.mogumarket.user.dto.response;

import com.lionkit.mogumarket.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class SignUpResponseDto {
    private Long id;
    private String username;

    /**
     * 유저의 회원가입과 함께 생성된
     * pointwallet 의 id 를 반환합니다.
     */
    private Long pointWalletId;

    public static SignUpResponseDto fromEntity(User user, Long pointWalletId ) {
        return SignUpResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .pointWalletId(pointWalletId)
                .build();
    }
}
