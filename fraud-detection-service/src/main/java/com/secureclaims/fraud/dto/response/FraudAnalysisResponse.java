package com.secureclaims.fraud.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FraudAnalysisResponse {
    private UUID claimId;
    private UUID userId;
    private int riskScore;
    private String riskLevel;
    private String analysisNotes;
    private LocalDateTime analyzedAt;
}
