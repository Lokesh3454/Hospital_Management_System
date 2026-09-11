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

    @Autowired
    private com.hospital.hms.repository.PatientRepository patientRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<InsuranceClaim>>> getAllClaims() {
        return ResponseEntity.ok(ApiResponse.ok("Insurance claims retrieved", insuranceService.getAllClaims()));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<List<InsuranceClaim>>> getClaimsByPatient(@PathVariable Long patientId) {
        enforcePatientPrivacy(patientId);
        return ResponseEntity.ok(ApiResponse.ok("Patient claims retrieved", insuranceService.getClaimsByPatient(patientId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> getClaimById(@PathVariable Long id) {
        InsuranceClaim claim = insuranceService.getClaimById(id);
        if (claim.getPatientId() != null) {
            enforcePatientPrivacy(claim.getPatientId());
        }
        return ResponseEntity.ok(ApiResponse.ok("Claim retrieved", claim));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> submitClaim(@RequestBody InsuranceClaim claim) {
        if (claim == null) {
            throw new com.hospital.hms.exception.BadRequestException("Insurance claim body is required");
        }
        if (claim.getPatientId() != null) {
            enforcePatientPrivacy(claim.getPatientId());
        }
        InsuranceClaim created = insuranceService.submitClaim(claim);
        return new ResponseEntity<>(ApiResponse.ok("Insurance claim submitted for pre-authorization", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> updateClaimStatus(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("status") || payload.get("status") == null) {
            throw new com.hospital.hms.exception.BadRequestException("status is required");
        }
        String statusStr = payload.get("status").toString().toUpperCase();
        BigDecimal approvedAmount = payload.get("approvedAmount") != null
                ? new BigDecimal(payload.get("approvedAmount").toString()) : null;
        BigDecimal patientCoPay = payload.get("patientCoPay") != null
                ? new BigDecimal(payload.get("patientCoPay").toString()) : null;
        String notes = payload.get("notes") != null ? payload.get("notes").toString() : null;

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

    private void enforcePatientPrivacy(Long requestedPatientId) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.hospital.hms.security.services.UserDetailsImpl userDetails) {
            boolean isStaff = auth.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_ADMIN") ||
                    a.getAuthority().equals("ROLE_DOCTOR") ||
                    a.getAuthority().equals("ROLE_RECEPTIONIST"));
            if (!isStaff) {
                com.hospital.hms.entity.Patient currentPatient = patientRepository.findByUserId(userDetails.getId())
                        .orElseThrow(() -> new com.hospital.hms.exception.BadRequestException("No patient profile found for this account."));
                if (!currentPatient.getId().equals(requestedPatientId)) {
                    throw new com.hospital.hms.exception.BadRequestException("Access denied: You can only view your own insurance claims.");
                }
            }
        }
    }
}
