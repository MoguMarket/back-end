package com.test.oauth.security.jwt.service;

import com.test.oauth.global.base.response.exception.BusinessException;
import com.test.oauth.global.base.response.exception.ExceptionType;
import com.test.oauth.security.jwt.enums.Role;
import com.test.oauth.security.jwt.dto.request.UsernameLoginRequestDto;
import com.test.oauth.security.jwt.dto.response.TokenResponseDto;
import com.test.oauth.security.jwt.entity.RefreshToken;
import com.test.oauth.security.jwt.repository.RefreshTokenRepository;
import com.test.oauth.security.jwt.util.JwtTokenProvider;
import com.test.oauth.user.entity.User;
import com.test.oauth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthService {


    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public TokenResponseDto usernameLogin(UsernameLoginRequestDto request) {

        /**
         * 유효한 username, pw 을 통한 로그인인지 확인
         * access token 과 refresh token 신규 발급
         * db 상 해당 유저와 해당 기기에 대한 기존 리프레시 토큰 존재 여부 확인 및 업데이트/새로 생성
         */
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        // db 에 이미 암호화된 형태로 저장되있기에 request 에 담긴 비밀번호를 암호화 하여 비교 수행
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ExceptionType.PASSWORD_NOT_MATCHED);
        }

        if (user.getRole() == Role.NOT_REGISTERED) {
            String tempAccessToken = issueTemporaryToken(user.getId());
            return TokenResponseDto.builder()
                    .access(tempAccessToken)
                    .refresh(null) // 정식 회원 가입까지 안되있으므로 장기 로그인을 위한 refresh token 은 불필요하여 굳이 발급 안함
                    .build();
        }

        // 최종 회원 가입까지 완료된 사용자인 경우 정식 access token 과 refresh token 발급해줌
        return issueTokensFor(user, request.getDeviceId());
    }

    /**
     * 임시 access token 발급을 위한 메서드
     * 최종 회원가입까진 완료하지 않은 유저들의 제한된 기능 이용을 위해 제공
     * @return
     */
    public String issueTemporaryToken(Long userId) {
        return jwtTokenProvider.generateAccessToken(userId);
    }


    // 얘는 refresh token 과는 별개로 특정 유저 & 기기에 대해 access token 과 refresh token 쌍 새로 발급/갱신
    /**
     * 최종 회원 가입 완료된 유저들에 대해 access token 과 refresh token 발급
     * @param user
     * @param deviceId
     * @return
     */
    public TokenResponseDto issueTokensFor(User user, String deviceId) {


        // access token 과 refresh token (재)발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        LocalDateTime expiresAt = jwtTokenProvider.extractExpiration(newRefreshToken);



        refreshTokenRepository.findByUserIdAndDeviceId(
                        user.getId(),
                        deviceId // 기기 식별자가 없는 로그인은 1개만 허용 : null 값도 값이기 때문
                )

                .map( existing -> {
                    existing.update(newRefreshToken, expiresAt); // 더티 채킹으로 업데이트 시 자동 저장됨
                    return existing;})
                .orElseGet(() -> {
                    RefreshToken refreshToken
                            =  RefreshToken.builder()
                            .token(newRefreshToken)
                            .user(user)
                            .expiresAt(expiresAt)
                            .deviceId(deviceId)
                            .build();
                    // 새로 저장
                    return refreshTokenRepository.save(refreshToken);

                });



        return TokenResponseDto.builder()
                .access(newAccessToken)
                .refresh(newRefreshToken)
                .build();
    }


    public TokenResponseDto issueTokensWithTemporaryAccessToken(String bearerToken, String deviceId) {
        String accessToken = JwtTokenProvider.extractToken(bearerToken);
        Long userId = jwtTokenProvider.extractId(accessToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        if (user.getRole() == Role.NOT_REGISTERED) {
            throw new BusinessException(ExceptionType.NOT_REGISTERED_USER);
        }

        return issueTokensFor(user, deviceId);
    }



    // 얘는 refresh token 을 사용한 갱신 목적으로 access token 과 refresh token 쌍 발급해주는거
    /**
     * refresh token 을 활용해  access token 신규 발급
     * rtr 전략을 활용해  refresh token도 신규 발급, 기존 refresh token 은 폐기
     */
    public TokenResponseDto reissueTokens(String bearerToken) {
        String refreshToken = JwtTokenProvider.extractToken(bearerToken);

        RefreshToken token = validateAndGetRefreshToken(refreshToken);

        User user = token.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        LocalDateTime newExpiry = jwtTokenProvider.extractExpiration(newRefreshToken);

        // 기존 토큰 업데이트
        token.update(newRefreshToken, newExpiry);
        refreshTokenRepository.save(token);

        return TokenResponseDto.builder()
                .access(newAccessToken)
                .refresh(newRefreshToken)
                .build();
    }

    /** 단일 로그아웃 */
    public void logout(String bearerToken) {
        String refreshToken = JwtTokenProvider.extractToken(bearerToken);
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    /** 전체 로그아웃 */
    public void logoutAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));
        refreshTokenRepository.deleteAllByUserId(user.getId());
    }



    private RefreshToken validateAndGetRefreshToken(String refreshToken){

        // 유효한 jwt token 인지 1차 검사
        if (!jwtTokenProvider.isValidToken(refreshToken)) {
            throw new BusinessException(ExceptionType.INVALID_REFRESH_TOKEN);
        }

        // db 상 유효한지 2차 검사
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .filter(rt -> !rt.isExpired())
                .orElseThrow(() -> new BusinessException(ExceptionType.REFRESH_TOKEN_EXPIRED));

        return token;

    }

}
