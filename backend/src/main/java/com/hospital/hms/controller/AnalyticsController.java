package com.hospital.hms.controller;

import com.hospital.hms.dto.dashboard.HospitalAnalyticsDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<HospitalAnalyticsDTO>> getExecutiveAnalytics() {
        HospitalAnalyticsDTO data = analyticsService.getExecutiveAnalytics();
        return ResponseEntity.ok(ApiResponse.ok("Executive hospital analytics retrieved successfully", data));
    }
}
