package com.secureclaims.fraud.controller;

import com.secureclaims.fraud.dto.response.ApiResponse;
import com.secureclaims.fraud.dto.response.FraudAnalysisResponse;
import com.secureclaims.fraud.service.FraudQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/fraud/v1")
@RequiredArgsConstructor
@Tag(name = "Fraud Detection", description = "Admin endpoints for fraud analysis results")
public class FraudAdminController {

    private final FraudQueryService fraudQueryService;

    @Operation(summary = "Get fraud analysis for a claim", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/admin/fraud/{claimId}")
    public ResponseEntity<ApiResponse<FraudAnalysisResponse>> getFraudAnalysis(@PathVariable final UUID claimId) {
        final FraudAnalysisResponse response = fraudQueryService.getFraudAnalysisByClaimId(claimId);
        return ResponseEntity.ok(ApiResponse.success(200, "Fraud analysis retrieved", response));
    }
}
