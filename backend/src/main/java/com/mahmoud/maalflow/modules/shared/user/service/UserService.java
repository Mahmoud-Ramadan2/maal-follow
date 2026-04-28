package com.mahmoud.maalflow.modules.shared.user.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.shared.enums.UserRole;
import com.mahmoud.maalflow.modules.shared.user.dto.UserRequest;
import com.mahmoud.maalflow.modules.shared.user.dto.UserResponse;
import com.mahmoud.maalflow.modules.shared.user.dto.UserSummary;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(UserRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (email == null) {
            throw new BusinessException("validation.email.required");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("validation.email.exists");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("validation.password.required");
        }

        User entity = new User();
        entity.setName(request.getName());
        entity.setEmail(email);
        entity.setPhone(request.getPhone());
        entity.setRole(request.getRole());
        entity.setPassword(passwordEncoder.encode(request.getPassword()));

        return toResponse(userRepository.save(entity));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));

        String email = normalizeEmail(request.getEmail());
        if (email != null && !email.equalsIgnoreCase(entity.getEmail())
                && userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("validation.email.exists");
        }

        entity.setName(request.getName());
        if (email != null) {
            entity.setEmail(email);
        }
        entity.setPhone(request.getPhone());
        entity.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<UserSummary> list(int page, int size, String search, UserRole role) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return userRepository.search(search, role, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public List<UserSummary> listUsers(List<UserRole> roles) {
        List<User> users = (roles == null || roles.isEmpty())
                ? userRepository.findAll()
                : userRepository.findByRoleIn(roles);

        return users.stream().map(this::toSummary).toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("messages.user.notFound");
        }
        userRepository.deleteById(id);
    }

    private UserSummary toSummary(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

