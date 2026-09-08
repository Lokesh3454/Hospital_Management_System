package com.hospital.hms.service.impl;

import com.hospital.hms.entity.InsuranceClaim;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.InsuranceClaimRepository;
import com.hospital.hms.service.InsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class InsuranceServiceImpl implements InsuranceService {

    @Autowired
    private InsuranceClaimRepository insuranceClaimRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceClaim> getAllClaims() {
        return insuranceClaimRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceClaim> getClaimsByPatient(Long patientId) {
        return insuranceClaimRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceClaim getClaimById(Long id) {
        return insuranceClaimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance claim not found with id " + id));
    }

    @Override
    public InsuranceClaim submitClaim(InsuranceClaim claim) {
        if (claim.getClaimNumber() == null || claim.getClaimNumber().isEmpty()) {
            claim.setClaimNumber("CLM-" + System.currentTimeMillis() % 100000);
        }
        if (claim.getSubmissionDate() == null) {
            claim.setSubmissionDate(LocalDate.now());
        }
        // Default co-pay calculation (standard 10% co-pay unless specified)
        if (claim.getPatientCoPay() == null || claim.getPatientCoPay().compareTo(BigDecimal.ZERO) == 0) {
            if (claim.getTotalBillAmount() != null) {
                claim.setPatientCoPay(claim.getTotalBillAmount().multiply(new BigDecimal("0.10")));
            }
        }
        if (claim.getClaimAmount() == null || claim.getClaimAmount().compareTo(BigDecimal.ZERO) == 0) {
            if (claim.getTotalBillAmount() != null) {
                claim.setClaimAmount(claim.getTotalBillAmount().subtract(claim.getPatientCoPay()));
            }
        }
        return insuranceClaimRepository.save(claim);
    }

    @Override
    public InsuranceClaim updateClaimStatus(Long id, InsuranceClaim.ClaimStatus status, BigDecimal approvedAmount, BigDecimal patientCoPay, String notes) {
        InsuranceClaim claim = getClaimById(id);
        claim.setStatus(status);
        if (approvedAmount != null) {
            claim.setApprovedAmount(approvedAmount);
        }
        if (patientCoPay != null) {
            claim.setPatientCoPay(patientCoPay);
        }
        if (notes != null && !notes.trim().isEmpty()) {
            String current = claim.getClaimNotes() != null ? claim.getClaimNotes() + " | " : "";
            claim.setClaimNotes(current + notes);
        }
        if (status == InsuranceClaim.ClaimStatus.SETTLED) {
            claim.setSettlementDate(LocalDate.now());
        }
        return insuranceClaimRepository.save(claim);
    }
}
