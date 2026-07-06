package com.secureclaims.events;

import org.springframework.context.ApplicationEvent;
import java.time.Instant;
import java.util.UUID;

public class FraudAnalysisCompletedEvent extends ApplicationEvent {

    private final UUID claimId;
    private final UUID userId;
    private final int riskScore;
    private final RiskLevel riskLevel;
    private final String analysisNotes;
    private final Instant analyzedAt;

    public FraudAnalysisCompletedEvent(Object source, UUID claimId, UUID userId,
                                        int riskScore, RiskLevel riskLevel,
                                        String analysisNotes, Instant analyzedAt) {
        super(source);
        this.claimId = claimId;
        this.userId = userId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.analysisNotes = analysisNotes;
        this.analyzedAt = analyzedAt;
    }

    public UUID getClaimId() { return claimId; }
    public UUID getUserId() { return userId; }
    public int getRiskScore() { return riskScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public String getAnalysisNotes() { return analysisNotes; }
    public Instant getAnalyzedAt() { return analyzedAt; }
}
