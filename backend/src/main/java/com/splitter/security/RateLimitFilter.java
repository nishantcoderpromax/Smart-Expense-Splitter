package com.splitter.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Rate-limits a fixed set of sensitive auth endpoints, keyed by client IP + path.
 * Deliberately only covers endpoints where abuse is meaningful (login brute-force,
 * registration spam, password-reset/verification token guessing) — most of the
 * API doesn't need this since it's already behind JWT auth.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;

    private record Rule(int maxAttempts, Duration window) {}

    private static final Map<String, Rule> RULES = Map.of(
            "/auth/login", new Rule(5, Duration.ofMinutes(1)),
            "/auth/register", new Rule(3, Duration.ofMinutes(10)),
            "/auth/forgot-password", new Rule(3, Duration.ofMinutes(10)),
            "/auth/resend-verification", new Rule(3, Duration.ofMinutes(10)),
            "/auth/reset-password", new Rule(5, Duration.ofMinutes(15)),
            "/auth/verify-email", new Rule(5, Duration.ofMinutes(15))
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Rule rule = RULES.get(request.getRequestURI());

        if (rule != null) {
            String key = clientIp(request) + ":" + request.getRequestURI();

            if (!rateLimiter.tryConsume(key, rule.maxAttempts(), rule.window())) {
                long retryAfterSeconds = rateLimiter.secondsUntilReset(key, rule.window());

                response.setStatus(429); // Too Many Requests
                response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(String.format(
                        "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\"," +
                                "\"message\":\"Too many attempts. Please try again in %d seconds.\"}",
                        LocalDateTime.now(), retryAfterSeconds));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        // Behind a reverse proxy/load balancer, the real client IP is in this
        // header instead of getRemoteAddr(), which would just show the proxy's IP.
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}