package com.hospital.hms.controller;

import com.hospital.hms.dto.dashboard.AdminDashboardDTO;
import com.hospital.hms.dto.dashboard.DoctorDashboardDTO;
import com.hospital.hms.dto.dashboard.PatientDashboardDTO;
import com.hospital.hms.dto.dashboard.ReceptionistDashboardDTO;
import com.hospital.hms.security.services.UserDetailsImpl;
import com.hospital.hms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        AdminDashboardDTO dto = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<DoctorDashboardDTO> getDoctorDashboard(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long authUserId = userDetails != null ? userDetails.getId() : null;
        DoctorDashboardDTO dto = dashboardService.getDoctorDashboard(authUserId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<PatientDashboardDTO> getPatientDashboard(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long authUserId = userDetails != null ? userDetails.getId() : null;
        PatientDashboardDTO dto = dashboardService.getPatientDashboard(authUserId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/receptionist")
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('ADMIN')")
    public ResponseEntity<ReceptionistDashboardDTO> getReceptionistDashboard() {
        ReceptionistDashboardDTO dto = dashboardService.getReceptionistDashboard();
        return ResponseEntity.ok(dto);
    }
}
