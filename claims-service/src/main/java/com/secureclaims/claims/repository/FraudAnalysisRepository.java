package com.secureclaims.claims.repository;

import com.secureclaims.claims.entity.FraudAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for FraudAnalysis entity operations.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Repository
public interface FraudAnalysisRepository extends JpaRepository<FraudAnalysis, UUID> {

    Optional<FraudAnalysis> findByClaimId(UUID claimId);
}
