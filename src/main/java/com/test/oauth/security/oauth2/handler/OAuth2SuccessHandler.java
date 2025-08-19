package com.test.oauth.security.oauth2.handler;

import com.test.oauth.security.jwt.enums.Role;
import com.test.oauth.security.jwt.service.AuthService;
import com.test.oauth.security.oauth2.principal.PrincipalDetails;
import com.test.oauth.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${oauth2.url.base}")
    private String BASE_URL;

    @Value("${oauth2.url.path.signup}")
    private String SIGNUP_PATH;

    @Value("${oauth2.url.path.auth}")
    private String AUTH_PATH;

    private final AuthService authService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        User user = principalDetails.getUser();

        /*  */
        /**
         * 디바이스 정보 추출
         * 디바이스 정보는 프론트에서 발급 및 localstorage 에 넣어줘야된다
         * 넘겨주지 않으면 걍 null 값 나오고, 그렇다고 해서 백엔드 상 딱히 에러가 나오게 구현하진 않았다. ( 신원 미상 처리 함 )
         */
        String deviceId = request.getHeader("Device-Id");

        String redirectUrl = getRedirectUrlByRole(user, deviceId);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

    }

    private String getRedirectUrlByRole(User user, String deviceId) {
        Role role = user.getRole();
        Long userId = user.getId();


        String tempAccessToken = authService.issueTemporaryToken(userId);


        /**
         * 최종 회원가입 이전의 사용자는 최종 회원가입 경로로의 접근을 위한 임시 access token 만 발급해줌
         * 여기까지 왔는데 회원가입 되지 않았다의 의미 : provider 상에 등록 완료됬고, 우리 db에 저장까지 됬는데 추가적인 부가정보를 입력하지 않아 우리 애플리케이션 상에서 최종 회원가입 완료는 안된 상태
         */


        if (role == Role.NOT_REGISTERED) {
            return UriComponentsBuilder
                    .fromUriString(BASE_URL + SIGNUP_PATH) // fromUriString -> 상대경로 포함 가능
                    .queryParam("id", userId)
                    .queryParam("access", tempAccessToken)
                    .build()
                    .toUriString();
        }

        /**
         * 최종 회원가입된 사용자에 대한 리다이렉트 :
         * 리다이렉트에선 헤더 조작 못하기 때문에 여기선 access token 만 발급해주고,
         * 프론트 단에선 여기서 받은 access token 을 활용해 refresh token 발급 요청 해서 따로 받아야됨
         * ( 물론 쿼리 파라미터로 refresh 까지 같이 보내게 구현할 수 있는데 그러면 보안상 취약 )
         */


        return UriComponentsBuilder
                .fromHttpUrl(BASE_URL + AUTH_PATH) // fromHttpUrl() -> 절대 경로만 허용
                .queryParam("id", userId)
                .queryParam("access", tempAccessToken)
                .build()
                .toUriString();
    }
}