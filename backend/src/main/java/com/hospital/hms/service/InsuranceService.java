package com.hospital.hms.service;

import com.hospital.hms.entity.InsuranceClaim;
import java.math.BigDecimal;
import java.util.List;

public interface InsuranceService {
    List<InsuranceClaim> getAllClaims();
    List<InsuranceClaim> getClaimsByPatient(Long patientId);
    InsuranceClaim getClaimById(Long id);
    InsuranceClaim submitClaim(InsuranceClaim claim);
    InsuranceClaim updateClaimStatus(Long id, InsuranceClaim.ClaimStatus status, BigDecimal approvedAmount, BigDecimal patientCoPay, String notes);
}
