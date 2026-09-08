package com.hospital.hms.controller;

import com.hospital.hms.dto.DoctorDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.entity.DoctorStatus;
import com.hospital.hms.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getAllDoctors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) DoctorStatus status) {
        List<DoctorDTO> doctors = doctorService.searchAndFilter(search, name, specialization, experience, status);
        return ResponseEntity.ok(ApiResponse.ok("Doctors retrieved successfully", doctors));
    }

    @GetMapping("/specializations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<String>>> getSpecializations() {
        List<String> specializations = doctorService.getAllSpecializations();
        return ResponseEntity.ok(ApiResponse.ok("Specializations retrieved successfully", specializations));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DoctorDTO>> getDoctorById(@PathVariable Long id) {
        DoctorDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.ok("Doctor retrieved successfully", doctor));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorDTO>> addDoctor(@Valid @RequestBody DoctorDTO doctorDTO) {
        DoctorDTO created = doctorService.addDoctor(doctorDTO);
        return new ResponseEntity<>(ApiResponse.ok("Doctor added successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorDTO>> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorDTO doctorDTO) {
        DoctorDTO updated = doctorService.updateDoctor(id, doctorDTO);
        return ResponseEntity.ok(ApiResponse.ok("Doctor updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.ok("Doctor deleted successfully", null));
    }
}
