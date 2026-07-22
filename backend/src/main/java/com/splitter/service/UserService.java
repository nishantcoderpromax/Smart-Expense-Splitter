package com.splitter.service;

import com.splitter.dto.UpdateProfileRequest;
import com.splitter.dto.UserProfileResponse;
import com.splitter.entity.User;
import com.splitter.exception.ApiException;
import com.splitter.repository.UserRepository;
import com.splitter.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
 
@Service
@RequiredArgsConstructor
public class UserService {
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
 
    public UserProfileResponse getProfile() {
        return toResponse(currentUser());
    }
 
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUser();
 
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
 
        // only touch the password if the user is actually trying to change it
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
 
        userRepository.save(user);
        return toResponse(user);
    }
 
    private User currentUser() {
        return userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
 
    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.isEmailVerified(), user.getCreatedAt());
    }
}