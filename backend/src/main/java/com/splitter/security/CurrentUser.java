package com.splitter.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {
    private CurrentUser() {}

    public static String email() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    }
}
