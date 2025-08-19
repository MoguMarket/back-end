package com.lionkit.mogumarket.security.jwt.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lionkit.mogumarket.global.base.response.ResponseUtil;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인은 했지만 권한이 부족하여 접근 튕길때의 처리 담당
 * (예: NOT_REGISTERED 권한 사용자가 USER 권한 path 접근 시도하여 예외 발생 )
 *
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        var body = ResponseUtil.createFailureResponse(ExceptionType.NOT_REGISTERED_USER);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
