package com.secureclaims.events;

import org.springframework.context.ApplicationEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ClaimCreatedEvent extends ApplicationEvent {

    private final UUID claimId;
    private final UUID userId;
    private final String policyNumber;
    private final String claimType;
    private final BigDecimal claimedAmount;
    private final int policyAgeMonths;
    private final Instant submittedAt;

    public ClaimCreatedEvent(Object source, UUID claimId, UUID userId, String policyNumber,
                             String claimType, BigDecimal claimedAmount, int policyAgeMonths,
                             Instant submittedAt) {
        super(source);
        this.claimId = claimId;
        this.userId = userId;
        this.policyNumber = policyNumber;
        this.claimType = claimType;
        this.claimedAmount = claimedAmount;
        this.policyAgeMonths = policyAgeMonths;
        this.submittedAt = submittedAt;
    }

    public UUID getClaimId() { return claimId; }
    public UUID getUserId() { return userId; }
    public String getPolicyNumber() { return policyNumber; }
    public String getClaimType() { return claimType; }
    public BigDecimal getClaimedAmount() { return claimedAmount; }
    public int getPolicyAgeMonths() { return policyAgeMonths; }
    public Instant getSubmittedAt() { return submittedAt; }
}
