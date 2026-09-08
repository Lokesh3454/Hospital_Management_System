package com.hospital.hms.controller;

import com.hospital.hms.dto.PatientDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getAllPatients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) Long doctorId) {
        List<PatientDTO> patients = patientService.searchAndFilter(search, name, phone, email, gender, bloodGroup, doctorId);
        return ResponseEntity.ok(ApiResponse.ok("Patients retrieved successfully", patients));
    }

    @Autowired
    private com.hospital.hms.repository.PatientRepository patientRepository;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientById(@PathVariable Long id) {
        enforcePatientPrivacy(id);
        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.ok("Patient retrieved successfully", patient));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientDTO>> addPatient(@Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO created = patientService.addPatient(patientDTO);
        return new ResponseEntity<>(ApiResponse.ok("Patient added successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<PatientDTO>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO patientDTO) {
        enforcePatientPrivacy(id);
        PatientDTO updated = patientService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(ApiResponse.ok("Patient updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.ok("Patient deleted successfully", null));
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
                    throw new com.hospital.hms.exception.BadRequestException("Access denied: You can only view or edit your own patient profile.");
                }
            }
        }
    }
}
