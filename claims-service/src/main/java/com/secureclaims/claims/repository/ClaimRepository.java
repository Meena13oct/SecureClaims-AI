package com.secureclaims.claims.repository;

import com.secureclaims.claims.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository for Claim entity operations.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    Page<Claim> findByUserId(UUID userId, Pageable pageable);

    long countByUserIdAndSubmittedAtAfter(UUID userId, LocalDateTime after);
}
