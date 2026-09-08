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

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<VitalsDTO>>> getPatientVitals(@PathVariable Long patientId) {
        List<VitalsDTO> list = vitalsService.getVitalsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok("Vitals history retrieved successfully", list));
    }

    @GetMapping("/patient/{patientId}/latest")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<VitalsDTO>> getLatestVitals(@PathVariable Long patientId) {
        VitalsDTO latest = vitalsService.getLatestVitalsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok("Latest vitals retrieved successfully", latest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVitals(@PathVariable Long id) {
        vitalsService.deleteVitals(id);
        return ResponseEntity.ok(ApiResponse.ok("Vitals record deleted successfully", null));
    }
}
