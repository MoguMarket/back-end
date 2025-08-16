package com.lionkit.mogumarket.security.oauth2.info;

import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.security.oauth2.enums.ProviderType;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, Map<String, Object> attributes) {
        switch (providerType) {
            case KAKAO -> {
                // 카카오의 경우 attributes 에 바로 email 과 nickname 을 담지 않고 account 와 profile 내부에 담기에 account 와 profile 을 구해놔야됨
                Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) account.get("profile");



                return OAuth2UserInfo.builder()
                        .providerType(providerType)
                        .attributes(attributes)
                        .providerId(String.valueOf(attributes.get("id")))
                        .nickname((String) profile.get("nickname"))
                        .email((String) account.get("email"))
                        .build();
            }
            case NAVER -> {
                //  naver 의 경우 응답 자체가 response 내부에 있음
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");

                return OAuth2UserInfo.builder()
                        .providerType(providerType)
                        .attributes(response)
                        .providerId((String) response.get("id"))
                        .nickname((String) response.get("name"))
                        .email((String) response.get("email"))
                        .build();


            }
            case GOOGLE -> {
                return OAuth2UserInfo.builder()
                        .providerType(providerType)
                        .attributes(attributes)
                        .providerId((String) attributes.get("sub"))
                        .nickname((String) attributes.get("name"))
                        .email((String) attributes.get("email"))
                        .build();
            }
        }
        throw new BusinessException(ExceptionType.INVALID_PROVIDER_TYPE_ERROR);

    }
}
