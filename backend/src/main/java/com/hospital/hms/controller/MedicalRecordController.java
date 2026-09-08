package com.hospital.hms.controller;

import com.hospital.hms.dto.MedicalRecordRequestDTO;
import com.hospital.hms.dto.MedicalRecordResponseDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.security.services.UserDetailsImpl;
import com.hospital.hms.service.MedicalRecordService;
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
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDTO>> createRecord(
            @Valid @RequestBody MedicalRecordRequestDTO request) {
        Long authenticatedUserId = getAuthenticatedUserId();
        MedicalRecordResponseDTO created = medicalRecordService.createRecord(request, authenticatedUserId);
        return new ResponseEntity<>(ApiResponse.ok("Medical record created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDTO>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordRequestDTO request) {
        Long authenticatedUserId = getAuthenticatedUserId();
        MedicalRecordResponseDTO updated = medicalRecordService.updateRecord(id, request, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Medical record updated successfully", updated));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDTO>> getRecordById(@PathVariable Long id) {
        Long authenticatedUserId = getAuthenticatedUserId();
        MedicalRecordResponseDTO record = medicalRecordService.getRecordById(id, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Medical record retrieved successfully", record));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponseDTO>>> getPatientHistory(@PathVariable Long patientId) {
        Long authenticatedUserId = getAuthenticatedUserId();
        List<MedicalRecordResponseDTO> history = medicalRecordService.getPatientHistory(patientId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Patient medical history retrieved successfully", history));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponseDTO>>> getDoctorRecords(@PathVariable Long doctorId) {
        List<MedicalRecordResponseDTO> records = medicalRecordService.getDoctorRecords(doctorId);
        return ResponseEntity.ok(ApiResponse.ok("Doctor medical records retrieved successfully", records));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponseDTO>>> getRecords(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        Long authenticatedUserId = getAuthenticatedUserId();
        List<MedicalRecordResponseDTO> records = medicalRecordService.searchAndFilter(search, date, patientId, doctorId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Medical records retrieved successfully", records));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        Long authenticatedUserId = getAuthenticatedUserId();
        medicalRecordService.deleteRecord(id, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Medical record deleted successfully", null));
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> clearAllHistory() {
        medicalRecordService.deleteAllRecords();
        return ResponseEntity.ok(ApiResponse.ok("All medical records history cleared successfully", null));
    }

    @PostMapping("/seed-sample-data")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponseDTO>>> seedSampleData() {
        medicalRecordService.seedDiverseSampleRecords();
        Long authenticatedUserId = getAuthenticatedUserId();
        List<MedicalRecordResponseDTO> records = medicalRecordService.searchAndFilter(null, null, null, null, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.ok("Sample medical records seeded successfully with diverse patients, doctors, and diseases", records));
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        return null;
    }
}
