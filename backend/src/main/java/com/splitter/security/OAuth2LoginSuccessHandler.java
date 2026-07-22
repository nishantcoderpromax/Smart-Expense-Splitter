package com.splitter.security;

import com.splitter.dto.AuthResponse;
import com.splitter.entity.User;
import com.splitter.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
 
import java.io.IOException;
 
/**
 * After Google authenticates the user, Spring Security calls this instead of
 * redirecting to some default page. We don't use Google's session at all past
 * this point — we mint our own access/refresh JWT pair (same shape as normal
 * email/password login) and hand the browser off to the frontend with those
 * tokens in the URL, since the frontend is a separate SPA that only understands
 * our own token format, not an OAuth2 session cookie.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
 
    private final AuthService authService;
 
    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;
 
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
 
        User user = authService.findOrCreateOAuthUser(email, name != null ? name : email);
        AuthResponse tokens = authService.buildAuthResponse(user);
 
        String target = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", tokens.getAccessToken())
                .queryParam("refreshToken", tokens.getRefreshToken())
                .queryParam("name", tokens.getName())
                .queryParam("email", tokens.getEmail())
                .build().toUriString();
 
        response.sendRedirect(target);
    }
}