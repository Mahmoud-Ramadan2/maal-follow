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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse create(UserRequest request) {
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("validation.email.exists");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("validation.password.required");
        }

        User entity = new User();
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setRole(request.getRole());
        // TODO: replace with PasswordEncoder when auth module is finalized.
        entity.setPassword(request.getPassword());

        return toResponse(userRepository.save(entity));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(entity.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("validation.email.exists");
        }

        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            // TODO: replace with PasswordEncoder when auth module is finalized.
            entity.setPassword(request.getPassword());
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
}

