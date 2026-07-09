package com.secureclaims.fraud.service;

import com.secureclaims.fraud.dto.response.FraudAnalysisResponse;
import java.util.UUID;

public interface FraudQueryService {
    FraudAnalysisResponse getFraudAnalysisByClaimId(UUID claimId);
}
