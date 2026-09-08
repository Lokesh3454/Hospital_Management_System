package com.hospital.hms.controller;

import com.hospital.hms.dto.ClinicalSupportDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.service.ClinicalSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/clinical-support")
public class ClinicalSupportController {

    @Autowired
    private ClinicalSupportService clinicalSupportService;

    @PostMapping("/check-safety")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<ClinicalSupportDTO.SafetyAlert>>> checkSafety(
            @RequestBody ClinicalSupportDTO.SafetyCheckRequest request) {
        List<ClinicalSupportDTO.SafetyAlert> alerts = clinicalSupportService.checkDrugSafety(request);
        return ResponseEntity.ok(ApiResponse.ok("Drug-Drug interaction and allergy contraindication check completed", alerts));
    }

    @GetMapping("/patient-brief/{patientId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<ClinicalSupportDTO.ClinicalBrief>> getPatientBrief(
            @PathVariable Long patientId) {
        ClinicalSupportDTO.ClinicalBrief brief = clinicalSupportService.generatePatientBrief(patientId);
        return ResponseEntity.ok(ApiResponse.ok("AI clinical summary brief generated", brief));
    }
}
