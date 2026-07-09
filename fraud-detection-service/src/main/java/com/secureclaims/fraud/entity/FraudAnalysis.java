package com.secureclaims.fraud.entity;

import com.secureclaims.events.RiskLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fraud analysis result entity (read-only in this service).
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Entity
@Table(name = "fraud_analyses", schema = "fraud")
@Getter
@Setter
@NoArgsConstructor
public class FraudAnalysis {

    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "analysis_notes", columnDefinition = "TEXT")
    private String analysisNotes;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
}
