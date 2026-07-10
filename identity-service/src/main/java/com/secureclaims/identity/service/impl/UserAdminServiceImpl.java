package com.secureclaims.identity.service.impl;

import com.secureclaims.identity.dto.response.UserResponse;
import com.secureclaims.identity.entity.Role;
import com.secureclaims.identity.entity.User;
import com.secureclaims.identity.repository.UserRepository;
import com.secureclaims.identity.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Implementation of admin user management operations.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;

    /**
     * Retrieve a paginated list of all registered users.
     *
     * @param pageable pagination parameters
     * @return page of user responses
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(final Pageable pageable) {
        log.info("Fetching all users, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable).map(this::mapToUserResponse);
    }

    private UserResponse mapToUserResponse(final User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
