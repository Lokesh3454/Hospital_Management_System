package com.hospital.hms.controller;

import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.entity.EmergencyCase;
import com.hospital.hms.service.EmergencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {

    @Autowired
    private EmergencyService emergencyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<EmergencyCase>>> getAllCases() {
        return ResponseEntity.ok(ApiResponse.ok("Emergency cases retrieved", emergencyService.getAllCases()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<EmergencyCase>>> getActiveCases() {
        return ResponseEntity.ok(ApiResponse.ok("Active trauma and emergency cases retrieved", emergencyService.getActiveCases()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<EmergencyCase>> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Emergency case retrieved", emergencyService.getCaseById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<EmergencyCase>> createCase(@RequestBody EmergencyCase emergencyCase) {
        EmergencyCase created = emergencyService.createCase(emergencyCase);
        return new ResponseEntity<>(ApiResponse.ok("Emergency patient triaged successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/triage")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<EmergencyCase>> updateTriage(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("triageLevel") || payload.get("triageLevel") == null) {
            throw new com.hospital.hms.exception.BadRequestException("triageLevel is required");
        }
        String levelStr = payload.get("triageLevel").toString().toUpperCase();
        String notes = payload.get("notes") != null ? payload.get("notes").toString() : null;
        EmergencyCase.TriageLevel level = EmergencyCase.TriageLevel.valueOf(levelStr);
        EmergencyCase updated = emergencyService.updateTriage(id, level, notes);
        return ResponseEntity.ok(ApiResponse.ok("Triage scoring updated", updated));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<EmergencyCase>> updateStatus(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("status") || payload.get("status") == null) {
            throw new com.hospital.hms.exception.BadRequestException("status is required");
        }
        String statusStr = payload.get("status").toString().toUpperCase();
        String bedNumber = payload.get("assignedBedNumber") != null ? payload.get("assignedBedNumber").toString() : null;
        EmergencyCase.EmergencyStatus status = EmergencyCase.EmergencyStatus.valueOf(statusStr);
        EmergencyCase updated = emergencyService.updateStatus(id, status, bedNumber);
        return ResponseEntity.ok(ApiResponse.ok("Emergency status updated", updated));
    }

    @PostMapping("/{id}/code-blue")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<EmergencyCase>> triggerCodeBlue(@PathVariable Long id) {
        EmergencyCase updated = emergencyService.triggerCodeBlue(id);
        return ResponseEntity.ok(ApiResponse.ok("🚨 CODE BLUE ALARM BROADCASTED TO ALL ON-DUTY TEAMS", updated));
    }
}
