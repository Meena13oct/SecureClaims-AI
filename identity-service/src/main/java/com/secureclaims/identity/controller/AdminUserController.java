package com.secureclaims.identity.controller;

import com.secureclaims.identity.dto.response.ApiResponse;
import com.secureclaims.identity.dto.response.UserResponse;
import com.secureclaims.identity.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin controller for user management operations.
 * Implements US-016: Admin List All Users.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/identity/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "Admin endpoints for user management")
public class AdminUserController {

    private final UserAdminService userAdminService;

    /**
     * Retrieve a paginated list of all registered users.
     *
     * @param pageable pagination parameters
     * @return paginated list of users
     */
    @Operation(summary = "List all registered users", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - USER role tokens blocked")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(final Pageable pageable) {

        log.info("Admin request: listing all users, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        final Page<UserResponse> users = userAdminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Users retrieved successfully", users));
    }
}
