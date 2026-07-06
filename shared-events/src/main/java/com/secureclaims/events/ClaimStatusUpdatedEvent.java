package com.secureclaims.events;

import org.springframework.context.ApplicationEvent;
import java.time.Instant;
import java.util.UUID;

public class ClaimStatusUpdatedEvent extends ApplicationEvent {

    private final UUID claimId;
    private final UUID userId;
    private final ClaimStatus previousStatus;
    private final ClaimStatus newStatus;
    private final String updatedBy;
    private final Instant updatedAt;

    public ClaimStatusUpdatedEvent(Object source, UUID claimId, UUID userId,
                                    ClaimStatus previousStatus, ClaimStatus newStatus,
                                    String updatedBy, Instant updatedAt) {
        super(source);
        this.claimId = claimId;
        this.userId = userId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public UUID getClaimId() { return claimId; }
    public UUID getUserId() { return userId; }
    public ClaimStatus getPreviousStatus() { return previousStatus; }
    public ClaimStatus getNewStatus() { return newStatus; }
    public String getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}
