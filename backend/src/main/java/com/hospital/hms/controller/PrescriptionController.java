package com.hospital.hms.controller;

import com.hospital.hms.dto.PrescriptionRequestDTO;
import com.hospital.hms.dto.PrescriptionResponseDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.security.services.UserDetailsImpl;
import com.hospital.hms.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> createPrescription(
            @Valid @RequestBody PrescriptionRequestDTO request) {
        Long authenticatedUserId = getAuthenticatedUserId();
        PrescriptionResponseDTO created = prescriptionService.createPrescription(request, authenticatedUserId);
        return new ResponseEntity<>(ApiResponse.ok("Prescription issued successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionRequestDTO request) {
        Long authenticatedUserId = getAuthenticatedUserId();
        PrescriptionResponseDTO updated = prescriptionService.updatePrescription(id, request, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Prescription updated successfully", updated));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> getPrescriptionById(@PathVariable Long id) {
        Long authenticatedUserId = getAuthenticatedUserId();
        PrescriptionResponseDTO prescription = prescriptionService.getPrescriptionById(id, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Prescription retrieved successfully", prescription));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getPatientPrescriptions(@PathVariable Long patientId) {
        Long authenticatedUserId = getAuthenticatedUserId();
        List<PrescriptionResponseDTO> list = prescriptionService.getPatientPrescriptions(patientId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Patient prescriptions retrieved successfully", list));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getDoctorPrescriptions(@PathVariable Long doctorId) {
        List<PrescriptionResponseDTO> list = prescriptionService.getDoctorPrescriptions(doctorId);
        return ResponseEntity.ok(ApiResponse.ok("Doctor prescriptions retrieved successfully", list));
    }

    @Autowired
    private com.hospital.hms.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.hospital.hms.repository.PatientRepository patientRepository;

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getAppointmentPrescriptions(@PathVariable Long appointmentId) {
        Long authenticatedUserId = getAuthenticatedUserId();
        if (authenticatedUserId != null) {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isStaff = auth.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_ADMIN") ||
                    a.getAuthority().equals("ROLE_DOCTOR") ||
                    a.getAuthority().equals("ROLE_RECEPTIONIST"));
            if (!isStaff) {
                com.hospital.hms.entity.Patient currentPatient = patientRepository.findByUserId(authenticatedUserId)
                        .orElseThrow(() -> new com.hospital.hms.exception.BadRequestException("No patient profile found for this account."));
                com.hospital.hms.entity.Appointment appt = appointmentRepository.findById(appointmentId)
                        .orElseThrow(() -> new com.hospital.hms.exception.ResourceNotFoundException("Appointment not found with id: " + appointmentId));
                if (!appt.getPatient().getId().equals(currentPatient.getId())) {
                    throw new com.hospital.hms.exception.BadRequestException("Access denied: You can only view prescriptions for your own appointments.");
                }
            }
        }
        List<PrescriptionResponseDTO> list = prescriptionService.getAppointmentPrescriptions(appointmentId);
        return ResponseEntity.ok(ApiResponse.ok("Appointment prescriptions retrieved successfully", list));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getPrescriptions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        Long authenticatedUserId = getAuthenticatedUserId();
        List<PrescriptionResponseDTO> list = prescriptionService.searchAndFilter(search, date, patientId, doctorId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Prescriptions retrieved successfully", list));
    }

    @PostMapping("/seed")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> seedSamplePrescriptions() {
        prescriptionService.seedDiverseSamplePrescriptions();
        return ResponseEntity.ok(ApiResponse.ok("Sample prescriptions seeded successfully", "8 diverse clinical prescriptions created."));
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        return null;
    }
}
