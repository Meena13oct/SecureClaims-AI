package com.secureclaims.fraud.service.impl;

import com.secureclaims.events.RiskLevel;
import com.secureclaims.fraud.dto.response.FraudAnalysisResponse;
import com.secureclaims.fraud.entity.FraudAnalysis;
import com.secureclaims.fraud.exception.ResourceNotFoundException;
import com.secureclaims.fraud.repository.FraudAnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FraudQueryServiceImpl.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class FraudQueryServiceImplTest {

    @Mock
    private FraudAnalysisRepository repository;

    @InjectMocks
    private FraudQueryServiceImpl fraudQueryService;

    @Test
    void should_returnFraudAnalysisResponse_when_claimIdExists() {
        // given
        final UUID claimId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final LocalDateTime analyzedAt = LocalDateTime.now();

        final FraudAnalysis analysis = new FraudAnalysis();
        analysis.setId(UUID.randomUUID());
        analysis.setClaimId(claimId);
        analysis.setUserId(userId);
        analysis.setRiskScore(7);
        analysis.setRiskLevel(RiskLevel.HIGH);
        analysis.setAnalysisNotes("Multiple red flags detected");
        analysis.setAnalyzedAt(analyzedAt);

        when(repository.findByClaimId(claimId)).thenReturn(Optional.of(analysis));

        // when
        final FraudAnalysisResponse response = fraudQueryService.getFraudAnalysisByClaimId(claimId);

        // then
        assertThat(response.getClaimId()).isEqualTo(claimId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getRiskScore()).isEqualTo(7);
        assertThat(response.getRiskLevel()).isEqualTo("HIGH");
        assertThat(response.getAnalysisNotes()).isEqualTo("Multiple red flags detected");
        assertThat(response.getAnalyzedAt()).isEqualTo(analyzedAt);
    }

    @Test
    void should_throwResourceNotFoundException_when_claimIdDoesNotExist() {
        // given
        final UUID claimId = UUID.randomUUID();
        when(repository.findByClaimId(claimId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> fraudQueryService.getFraudAnalysisByClaimId(claimId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("FraudAnalysis")
                .hasMessageContaining("claimId")
                .hasMessageContaining(claimId.toString());
    }

    @Test
    void should_mapAllRiskLevels_when_analysisHasDifferentLevels() {
        // given
        final UUID claimId = UUID.randomUUID();
        final FraudAnalysis analysis = new FraudAnalysis();
        analysis.setId(UUID.randomUUID());
        analysis.setClaimId(claimId);
        analysis.setUserId(UUID.randomUUID());
        analysis.setRiskScore(2);
        analysis.setRiskLevel(RiskLevel.LOW);
        analysis.setAnalysisNotes("No issues found");
        analysis.setAnalyzedAt(LocalDateTime.now());

        when(repository.findByClaimId(claimId)).thenReturn(Optional.of(analysis));

        // when
        final FraudAnalysisResponse response = fraudQueryService.getFraudAnalysisByClaimId(claimId);

        // then
        assertThat(response.getRiskLevel()).isEqualTo("LOW");
        assertThat(response.getRiskScore()).isEqualTo(2);
    }

    @Test
    void should_handleNullAnalysisNotes_when_notesAreEmpty() {
        // given
        final UUID claimId = UUID.randomUUID();
        final FraudAnalysis analysis = new FraudAnalysis();
        analysis.setId(UUID.randomUUID());
        analysis.setClaimId(claimId);
        analysis.setUserId(UUID.randomUUID());
        analysis.setRiskScore(4);
        analysis.setRiskLevel(RiskLevel.MEDIUM);
        analysis.setAnalysisNotes(null);
        analysis.setAnalyzedAt(LocalDateTime.now());

        when(repository.findByClaimId(claimId)).thenReturn(Optional.of(analysis));

        // when
        final FraudAnalysisResponse response = fraudQueryService.getFraudAnalysisByClaimId(claimId);

        // then
        assertThat(response.getAnalysisNotes()).isNull();
        assertThat(response.getRiskLevel()).isEqualTo("MEDIUM");
    }
}
