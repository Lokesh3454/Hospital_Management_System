package com.hospital.hms.controller;

import com.hospital.hms.dto.LabTestDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.service.LabTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/lab-tests")
public class LabTestController {

    @Autowired
    private LabTestService labTestService;

    @Autowired
    private com.hospital.hms.repository.PatientRepository patientRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<LabTestDTO>>> getAllLabTests() {
        List<LabTestDTO> list = labTestService.getAllLabTests();
        return ResponseEntity.ok(ApiResponse.ok("Diagnostic lab orders retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<LabTestDTO>> getLabTestById(@PathVariable Long id) {
        LabTestDTO dto = labTestService.getLabTestById(id);
        if (dto.getPatientId() != null) {
            enforcePatientPrivacy(dto.getPatientId());
        }
        return ResponseEntity.ok(ApiResponse.ok("Diagnostic test retrieved successfully", dto));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<LabTestDTO>>> getPatientLabTests(@PathVariable Long patientId) {
        enforcePatientPrivacy(patientId);
        List<LabTestDTO> list = labTestService.getLabTestsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok("Patient diagnostic orders retrieved successfully", list));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<LabTestDTO>>> getDoctorLabTests(@PathVariable Long doctorId) {
        List<LabTestDTO> list = labTestService.getLabTestsByDoctor(doctorId);
        return ResponseEntity.ok(ApiResponse.ok("Doctor diagnostic orders retrieved successfully", list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<LabTestDTO>> createLabTest(@RequestBody LabTestDTO dto) {
        LabTestDTO created = labTestService.createLabTest(dto);
        return new ResponseEntity<>(ApiResponse.ok("Lab order requested successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<LabTestDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        LabTestDTO updated = labTestService.updateLabTestStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Lab order status updated successfully", updated));
    }

    @PostMapping("/{id}/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<LabTestDTO>> recordResults(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String resultValue = payload.get("resultValue");
        String interpretation = payload.get("interpretation");
        String remarks = payload.get("remarks");
        LabTestDTO updated = labTestService.recordResults(id, resultValue, interpretation, remarks);
        return ResponseEntity.ok(ApiResponse.ok("Diagnostic test results published successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteLabTest(@PathVariable Long id) {
        labTestService.deleteLabTest(id);
        return ResponseEntity.ok(ApiResponse.ok("Lab test order deleted successfully", null));
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
                    throw new com.hospital.hms.exception.BadRequestException("Access denied: You can only view your own lab tests.");
                }
            }
        }
    }
}
