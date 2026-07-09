package com.secureclaims.claims.repository;

import com.secureclaims.claims.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Document entity operations.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByClaimId(UUID claimId);
}
