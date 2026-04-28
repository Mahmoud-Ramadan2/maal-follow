package com.mahmoud.maalflow.modules.shared.user.controller;

import com.mahmoud.maalflow.modules.shared.enums.UserRole;
import com.mahmoud.maalflow.modules.shared.user.dto.UserRequest;
import com.mahmoud.maalflow.modules.shared.user.dto.UserResponse;
import com.mahmoud.maalflow.modules.shared.user.dto.UserSummary;
import com.mahmoud.maalflow.modules.shared.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserSummary>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role
    ) {
        return ResponseEntity.ok(userService.list(page, size, search, role));
    }

    @GetMapping("/collectors")
    public ResponseEntity<List<UserSummary>> listUsers(
            @RequestParam(required = false) List<UserRole> roles
    ) {
        return ResponseEntity.ok(userService.listUsers(roles));
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        userService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
}

