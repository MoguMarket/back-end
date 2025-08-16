package com.lionkit.mogumarket.security.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lionkit.mogumarket.global.base.response.ResponseUtil;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 조차 안한 상태에서 접근 튕길때의 처리 담당
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        var body = ResponseUtil.createFailureResponse(ExceptionType.UNAUTHORIZED_USER);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}