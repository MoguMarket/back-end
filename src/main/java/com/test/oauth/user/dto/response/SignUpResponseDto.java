package com.test.oauth.user.dto.response;

import com.test.oauth.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponseDto {
    private Long id;
    private String username;

    public static SignUpResponseDto fromEntity(User user) {
        return SignUpResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
