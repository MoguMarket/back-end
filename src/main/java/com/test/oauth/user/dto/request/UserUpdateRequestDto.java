package com.test.oauth.user.dto.request;

import lombok.*;


/**
 * 부가적인 정보의 업데이트를 위한 dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDto {
    private String nickname;
    private String email;
}
