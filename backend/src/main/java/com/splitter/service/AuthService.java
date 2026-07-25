package com.splitter.service;

import com.splitter.dto.*;
import com.splitter.entity.RefreshToken;
import com.splitter.entity.User;
import com.splitter.entity.VerificationToken;
import com.splitter.enums.TokenPurpose;
import com.splitter.exception.ApiException;
import com.splitter.repository.RefreshTokenRepository;
import com.splitter.repository.UserRepository;
import com.splitter.repository.VerificationTokenRepository;
import com.splitter.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
 
import java.time.LocalDateTime;
import java.util.UUID;
 
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
 
    
 
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
 
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
 
    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;
 
    @Value("${app.frontend-url}")
    private String frontendUrl;
 
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false) // explicit: overrides the entity's default(true) for real registrations
                .build();
        userRepository.save(user);
 
        // A slow/misconfigured/blocked SMTP server should never prevent account
        // creation itself — sign-up succeeds regardless; if the email genuinely
        // fails, the person can just use "Resend verification email" later.
        try {
            issueAndSendVerificationEmail(user);
        } catch (Exception e) {
            log.warn("Could not send verification email to {}: {}", user.getEmail(), e.getMessage());
        }
 
        // Registration still logs the user in immediately — verification is
        // tracked/shown in the UI, but doesn't block using the app.
        return buildAuthResponse(user);
    }
 
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
 
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return buildAuthResponse(user);
    }
 
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
 
        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired, please log in again");
        }
        // Rotate: delete old, issue new access + refresh token
        refreshTokenRepository.delete(stored);
        return buildAuthResponse(stored.getUser());
    }
 
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }
 
    /**
     * Finds an existing account by email, or creates one for a first-time Google
     * sign-in. OAuth users never use a password to log in, but the users.password
     * column is NOT NULL, so we store a random bcrypt hash they can never guess
     * or use — it's just a placeholder to satisfy the schema. Google has already
     * verified this email address, so emailVerified defaults to true here.
     */
    public User findOrCreateOAuthUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .name(name)
                            .email(email)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .build(); // emailVerified stays at its default(true)
                    return userRepository.save(user);
                });
    }
 
    // ---------- Email verification ----------
 
    public void verifyEmail(String token) {
        VerificationToken vt = verificationTokenRepository.findByTokenAndPurpose(token, TokenPurpose.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid verification link"));
 
        if (vt.isUsed() || vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This verification link has expired or was already used");
        }
 
        User user = vt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
 
        vt.setUsed(true);
        verificationTokenRepository.save(vt);
    }
 
    public void resendVerificationEmail(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
 
        if (user.isEmailVerified()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This email is already verified");
        }
 
        verificationTokenRepository.deleteByUserIdAndPurpose(user.getId(), TokenPurpose.EMAIL_VERIFICATION);
 
        try {
            issueAndSendVerificationEmail(user);
        } catch (Exception e) {
            log.warn("Could not resend verification email to {}: {}", user.getEmail(), e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not send the email right now. Please try again in a moment.");
        }
    }
 
    private void issueAndSendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        verificationTokenRepository.save(VerificationToken.builder()
                .user(user)
                .token(token)
                .purpose(TokenPurpose.EMAIL_VERIFICATION)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build());
 
        String link = frontendUrl + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), link);
    }
 
    // ---------- Password reset ----------
 
    /** Always behaves identically whether or not the email exists, so callers
     *  can't use this endpoint to discover which emails are registered. */
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            verificationTokenRepository.deleteByUserIdAndPurpose(user.getId(), TokenPurpose.PASSWORD_RESET);
 
            String token = UUID.randomUUID().toString();
            verificationTokenRepository.save(VerificationToken.builder()
                    .user(user)
                    .token(token)
                    .purpose(TokenPurpose.PASSWORD_RESET)
                    .expiryDate(LocalDateTime.now().plusMinutes(30))
                    .build());
 
            String link = frontendUrl + "/reset-password?token=" + token;
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), link);
            } catch (Exception e) {
                log.warn("Could not send password reset email to {}: {}", user.getEmail(), e.getMessage());
                // deliberately swallowed — this endpoint always responds the same way
                // regardless of outcome, so it never reveals whether the email exists
                // or whether sending succeeded
            }
        });
    }
 
    public void resetPassword(String token, String newPassword) {
        VerificationToken vt = verificationTokenRepository.findByTokenAndPurpose(token, TokenPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset link"));
 
        if (vt.isUsed() || vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This reset link has expired or was already used");
        }
 
        User user = vt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
 
        vt.setUsed(true);
        verificationTokenRepository.save(vt);
 
        // force re-login on every device — a leaked/reused reset link shouldn't
        // leave old sessions valid after the password has changed
        refreshTokenRepository.deleteByUserId(user.getId());
    }
 
    /** Public so OAuth2LoginSuccessHandler can issue the same JWT/refresh-token pair. */
    public AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
 
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusNanos(refreshTokenExpiryMs * 1_000_000))
                .build();
        refreshTokenRepository.save(refreshToken);
 
        return new AuthResponse(accessToken, refreshToken.getToken(), user.getName(), user.getEmail());
    }
}

