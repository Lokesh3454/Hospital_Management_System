package com.hospital.hms.controller;

import com.hospital.hms.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<String>> allAccess() {
        return ResponseEntity.ok(ApiResponse.ok("Public Content: Hospital Management System API is online and operational."));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> patientAccess() {
        return ResponseEntity.ok(ApiResponse.ok("Patient Content: Access granted for Patient portal."));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<String>> doctorAccess() {
        return ResponseEntity.ok(ApiResponse.ok("Doctor Content: Access granted for Doctor workspace."));
    }

    @GetMapping("/receptionist")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<ApiResponse<String>> receptionistAccess() {
        return ResponseEntity.ok(ApiResponse.ok("Receptionist Content: Access granted for Receptionist front desk."));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminAccess() {
        return ResponseEntity.ok(ApiResponse.ok("Admin Content: Access granted for System Administrator console."));
    }
}
