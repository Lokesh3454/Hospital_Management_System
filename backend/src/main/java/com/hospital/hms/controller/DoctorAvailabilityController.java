package com.hospital.hms.controller;

import com.hospital.hms.dto.DoctorAvailabilityDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class DoctorAvailabilityController {

    @Autowired
    private DoctorAvailabilityService availabilityService;

    @GetMapping("/doctors/{doctorId}/availabilities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DoctorAvailabilityDTO>>> getAvailabilitiesByDoctor(@PathVariable Long doctorId) {
        List<DoctorAvailabilityDTO> list = availabilityService.getAvailabilitiesByDoctorId(doctorId);
        return ResponseEntity.ok(ApiResponse.ok("Doctor availability retrieved successfully", list));
    }

    @PostMapping("/doctors/{doctorId}/availabilities")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorAvailabilityDTO>> addAvailability(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorAvailabilityDTO dto) {
        DoctorAvailabilityDTO created = availabilityService.addAvailability(doctorId, dto);
        return new ResponseEntity<>(ApiResponse.ok("Availability slot added successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/availabilities/{availabilityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorAvailabilityDTO>> updateAvailability(
            @PathVariable Long availabilityId,
            @Valid @RequestBody DoctorAvailabilityDTO dto) {
        DoctorAvailabilityDTO updated = availabilityService.updateAvailability(availabilityId, dto);
        return ResponseEntity.ok(ApiResponse.ok("Availability slot updated successfully", updated));
    }

    @DeleteMapping("/availabilities/{availabilityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(@PathVariable Long availabilityId) {
        availabilityService.deleteAvailability(availabilityId);
        return ResponseEntity.ok(ApiResponse.ok("Availability slot deleted successfully", null));
    }
}
