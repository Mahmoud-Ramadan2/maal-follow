package com.mahmoud.maalflow.modules.shared.user.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.UserNotFoundException;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User management service.
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/shared/user/service/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("messages.user.notFound", id));
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long id, String name, String phone) {
        User user = getById(id);
        if (name != null) user.setName(name);
        if (phone != null) user.setPhone(phone);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getById(id);
        userRepository.delete(user);
        log.info("Deleted user ID: {}", id);
    }
}

