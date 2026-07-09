package com.secureclaims.fraud.repository;

import com.secureclaims.fraud.entity.FraudAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudAnalysisRepository extends JpaRepository<FraudAnalysis, UUID> {
    Optional<FraudAnalysis> findByClaimId(UUID claimId);
}
