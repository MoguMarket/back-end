package com.test.oauth.security.oauth2.info;

import com.test.oauth.security.oauth2.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;


/**
 * provider 가 넘긴 정보 저장용 편의 OAuth2 객체. dto.
 */
@Getter
@AllArgsConstructor
@Builder
public class OAuth2UserInfo {

    public final ProviderType providerType; // provider 벤더명


    // provider 가 넘긴 전체 정보
    private Map<String, Object> attributes;

    // 해당 provider 상의 식별자 값
    private String providerId;




    /**
     * 이외의 추가적으로 단축 호출 원하는 정보에 대해 필드 추가
     */
    // 해당 provider 상의 사용자명
    private String nickname;
    private String email;







}