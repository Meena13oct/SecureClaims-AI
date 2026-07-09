package com.secureclaims.fraud.service.impl;

import com.secureclaims.fraud.dto.response.FraudAnalysisResponse;
import com.secureclaims.fraud.entity.FraudAnalysis;
import com.secureclaims.fraud.exception.ResourceNotFoundException;
import com.secureclaims.fraud.repository.FraudAnalysisRepository;
import com.secureclaims.fraud.service.FraudQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraudQueryServiceImpl implements FraudQueryService {

    private final FraudAnalysisRepository repository;

    @Override
    @Transactional(readOnly = true)
    public FraudAnalysisResponse getFraudAnalysisByClaimId(final UUID claimId) {
        final FraudAnalysis analysis = repository.findByClaimId(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("FraudAnalysis", "claimId", claimId));
        return FraudAnalysisResponse.builder()
                .claimId(analysis.getClaimId())
                .userId(analysis.getUserId())
                .riskScore(analysis.getRiskScore())
                .riskLevel(analysis.getRiskLevel().name())
                .analysisNotes(analysis.getAnalysisNotes())
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
    }
}
