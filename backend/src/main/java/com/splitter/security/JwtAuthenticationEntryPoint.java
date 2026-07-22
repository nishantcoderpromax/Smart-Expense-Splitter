package com.splitter.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Without this, Spring Security's default behavior for a missing/invalid/expired
 * JWT is to return 403 Forbidden — which looks identical to "you're logged in but
 * not allowed to do this." The frontend's refresh-token logic only reacts to 401,
 * so unauthenticated requests would silently fail instead of triggering a refresh.
 * This makes "not authenticated" correctly return 401, reserving 403 for real
 * authorization failures (e.g. not a group admin).
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", "Missing or invalid access token");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}