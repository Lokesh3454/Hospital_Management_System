package com.hospital.hms.controller;

import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.entity.InsuranceClaim;
import com.hospital.hms.service.InsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/insurance")
public class InsuranceController {

    @Autowired
    private InsuranceService insuranceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InsuranceClaim>>> getAllClaims() {
        return ResponseEntity.ok(ApiResponse.ok("Insurance claims retrieved", insuranceService.getAllClaims()));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<InsuranceClaim>>> getClaimsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.ok("Patient claims retrieved", insuranceService.getClaimsByPatient(patientId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InsuranceClaim>> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Claim retrieved", insuranceService.getClaimById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> submitClaim(@RequestBody InsuranceClaim claim) {
        InsuranceClaim created = insuranceService.submitClaim(claim);
        return new ResponseEntity<>(ApiResponse.ok("Insurance claim submitted for pre-authorization", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> updateClaimStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String statusStr = (String) payload.get("status");
        BigDecimal approvedAmount = payload.get("approvedAmount") != null
                ? new BigDecimal(payload.get("approvedAmount").toString()) : null;
        BigDecimal patientCoPay = payload.get("patientCoPay") != null
                ? new BigDecimal(payload.get("patientCoPay").toString()) : null;
        String notes = (String) payload.get("notes");

        InsuranceClaim.ClaimStatus status = InsuranceClaim.ClaimStatus.valueOf(statusStr);
        InsuranceClaim updated = insuranceService.updateClaimStatus(id, status, approvedAmount, patientCoPay, notes);
        return ResponseEntity.ok(ApiResponse.ok("Insurance claim status updated", updated));
    }

    @PutMapping("/{id}/pre-auth")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> updatePreAuth(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) BigDecimal approvedAmount,
            @RequestParam(required = false) String notes) {
        InsuranceClaim.ClaimStatus claimStatus = InsuranceClaim.ClaimStatus.valueOf(status);
        InsuranceClaim claim = insuranceService.getClaimById(id);
        BigDecimal patientCoPay = claim.getPatientCoPay();
        if (approvedAmount != null && claim.getTotalBillAmount() != null) {
            patientCoPay = claim.getTotalBillAmount().subtract(approvedAmount);
            if (patientCoPay.compareTo(BigDecimal.ZERO) < 0) patientCoPay = BigDecimal.ZERO;
        }
        InsuranceClaim updated = insuranceService.updateClaimStatus(id, claimStatus, approvedAmount, patientCoPay, notes);
        return ResponseEntity.ok(ApiResponse.ok("Pre-authorization updated successfully", updated));
    }
}
