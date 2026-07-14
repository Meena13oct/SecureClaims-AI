package com.secureclaims.identity.service.impl;

import com.secureclaims.identity.dto.response.PagedResponse;
import com.secureclaims.identity.dto.response.UserResponse;
import com.secureclaims.identity.entity.Role;
import com.secureclaims.identity.entity.User;
import com.secureclaims.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserAdminServiceImpl.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminServiceImpl userAdminService;

    @Test
    void should_returnPagedUsers_when_usersExist() {
        // given
        final Pageable pageable = PageRequest.of(0, 10);
        final User user = createTestUser();
        final Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // when
        final PagedResponse<UserResponse> result = userAdminService.getAllUsers(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPage()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void should_returnEmptyPage_when_noUsersExist() {
        // given
        final Pageable pageable = PageRequest.of(0, 10);
        final Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // when
        final PagedResponse<UserResponse> result = userAdminService.getAllUsers(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void should_mapUserFieldsCorrectly_when_userHasRoles() {
        // given
        final Pageable pageable = PageRequest.of(0, 10);
        final User user = createTestUser();
        final Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // when
        final PagedResponse<UserResponse> result = userAdminService.getAllUsers(pageable);

        // then
        final UserResponse response = result.getContent().get(0);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("Test");
        assertThat(response.getLastName()).isEqualTo("User");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
    }

    private User createTestUser() {
        final User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordHash("$2a$10$hashedpassword");

        final Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        return user;
    }
}
