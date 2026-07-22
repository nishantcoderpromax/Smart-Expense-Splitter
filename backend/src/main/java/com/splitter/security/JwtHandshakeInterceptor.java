package com.splitter.security;


import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
 
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
 
/**
 * SockJS/STOMP handshakes are plain HTTP requests, so they don't carry an
 * Authorization header the way our normal REST calls do (browsers can't set
 * custom headers on a WebSocket upgrade). Instead the frontend appends the
 * access token as a query param (?token=...), and we validate it here before
 * the connection is allowed to open at all — an invalid/missing token gets
 * rejected with 401 before any socket is established.
 */
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
 
    private final JwtUtil jwtUtil;
 
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request.getURI().getQuery());
 
        if (token == null || !jwtUtil.isValid(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
 
        attributes.put("userEmail", jwtUtil.extractEmail(token));
        return true;
    }
 
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // nothing to do after the handshake completes
    }
 
    private String extractToken(String query) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                return URLDecoder.decode(param.substring("token=".length()), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}