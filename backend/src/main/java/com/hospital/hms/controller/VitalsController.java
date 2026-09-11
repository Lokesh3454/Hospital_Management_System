package com.hospital.hms.controller;

import com.hospital.hms.dto.VitalsDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.service.VitalsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/vitals")
public class VitalsController {

    @Autowired
    private VitalsService vitalsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<VitalsDTO>> recordVitals(@RequestBody VitalsDTO dto) {
        VitalsDTO saved = vitalsService.recordVitals(dto);
        return new ResponseEntity<>(ApiResponse.ok("Vitals recorded successfully", saved), HttpStatus.CREATED);
    }

    @Autowired
    private com.hospital.hms.repository.PatientRepository patientRepository;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<VitalsDTO>>> getPatientVitals(@PathVariable Long patientId) {
        enforcePatientPrivacy(patientId);
        List<VitalsDTO> list = vitalsService.getVitalsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok("Vitals history retrieved successfully", list));
    }

    @GetMapping("/patient/{patientId}/latest")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<VitalsDTO>> getLatestVitals(@PathVariable Long patientId) {
        enforcePatientPrivacy(patientId);
        VitalsDTO latest = vitalsService.getLatestVitalsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok("Latest vitals retrieved successfully", latest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVitals(@PathVariable Long id) {
        vitalsService.deleteVitals(id);
        return ResponseEntity.ok(ApiResponse.ok("Vitals record deleted successfully", null));
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
                    throw new com.hospital.hms.exception.BadRequestException("Access denied: You can only view your own vitals.");
                }
            }
        }
    }
}
