package com.mahmoud.maalflow.security;

import com.mahmoud.maalflow.exception.AccessDeniedException;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility to get the current authenticated user.
 *
 * USE THIS TO REPLACE ALL hardcoded userId=1 across the codebase.
 *
 * BEFORE (appears 15+ times):
 *   User currentUser = userRepository.findById(1L).orElse(null);
 *
 * AFTER:
 *   User currentUser = securityUtils.getCurrentUser();
 *
 * Copy to: src/main/java/com/mahmoud/maalflow/security/SecurityUtils.java
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Get the currently authenticated user entity.
     * @return User entity from database
     * @throws AccessDeniedException if not authenticated
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new AccessDeniedException("messages.auth.notAuthenticated");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("messages.auth.userNotFound"));
    }

    /**
     * Get the current user's ID.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if current user has a specific role.
     */
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if current user is ADMIN.
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}

