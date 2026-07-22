package com.splitter.controller;


import com.splitter.dto.UpdateProfileRequest;
import com.splitter.dto.UserProfileResponse;
import com.splitter.dto.UserSearchResult;
import com.splitter.repository.UserRepository;
import com.splitter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/users/me")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    @PutMapping("/users/me")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    /**
     * Lets the "Add Member" form find people by partial name/email instead of
     * requiring the exact address. Only returns id/name/email — never passwords.
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<UserSearchResult>> search(@RequestParam String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        List<UserSearchResult> results = userRepository.searchByNameOrEmail(query, PageRequest.of(0, 10))
                .stream()
                .map(u -> new UserSearchResult(u.getId(), u.getName(), u.getEmail()))
                .toList();
        return ResponseEntity.ok(results);
    }
}
