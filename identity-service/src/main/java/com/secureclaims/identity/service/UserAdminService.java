package com.secureclaims.identity.service;

import com.secureclaims.identity.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for admin user management operations.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
public interface UserAdminService {

    /**
     * Retrieve a paginated list of all registered users.
     *
     * @param pageable pagination parameters
     * @return page of user responses
     */
    Page<UserResponse> getAllUsers(Pageable pageable);
}
