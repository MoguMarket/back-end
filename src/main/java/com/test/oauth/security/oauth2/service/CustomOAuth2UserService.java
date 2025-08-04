package com.test.oauth.security.oauth2.service;




import com.test.oauth.security.jwt.enums.Role;
import com.test.oauth.security.oauth2.enums.ProviderType;
import com.test.oauth.security.oauth2.info.OAuth2UserInfo;
import com.test.oauth.security.oauth2.info.OAuth2UserInfoFactory;
import com.test.oauth.security.oauth2.principal.PrincipalDetails;
import com.test.oauth.user.entity.User;
import com.test.oauth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(
            OAuth2UserRequest userRequest // OAuth2UserRequest 에는 clientRegistration(설정 파일상 provider 측의 클라이언트,즉 우리의 등록 정보), accessToken 내장
    ) throws OAuth2AuthenticationException {

        // OAuth2UserRequest 에 내장된 clientRegistration 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // 해당 provider 명
        ProviderType providerType = ProviderType.from(registrationId); // 쓰기 편하게 enum 화
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName(); // 해당 provider 상의 식별자 key 명


        // OAuth2UserRequest 에 내장된 accesstoken 을 활용해 provider 로부터 OAuth2User(유저 정보)를 response 로 받음
        // OAuth2User 에는 attributes , authorities 내장
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // OAuth2User 에 내장된 attributes 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();


        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, attributes);

        User user = getOrSaveUser(providerType, oAuth2UserInfo);


        // Security context에 저장할 객체 생성
        return PrincipalDetails.builder()
                .user(user)
                .attributes(attributes)
                .build();
    }



    private User getOrSaveUser(ProviderType providerType,  OAuth2UserInfo oAuth2UserInfo) {

        String providerId = oAuth2UserInfo.getProviderId();

        Optional<User> optionalUser = userRepository.findByOAuthInfo(providerType, providerId);

        if (optionalUser.isEmpty()) {
            User unregisteredUser = User.builder()
                    .providerType(providerType)
                    .providerId(providerId)
                    .role(Role.NOT_REGISTERED) // NOT_REGISTERED 이면 회원가입 페이지로 redirect  , NOT_REGISTERED가 아니면 서비스 페이지로 redirect
                    .nickname(oAuth2UserInfo.getNickname())
                    .email(oAuth2UserInfo.getEmail())
                    .build();

            return userRepository.save(unregisteredUser);
        }

        return optionalUser.get();

    }
}