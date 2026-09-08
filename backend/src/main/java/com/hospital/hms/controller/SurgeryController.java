package com.hospital.hms.controller;

import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.entity.SurgerySchedule;
import com.hospital.hms.service.SurgeryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/surgery")
public class SurgeryController {

    @Autowired
    private SurgeryService surgeryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SurgerySchedule>>> getAllSurgeries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SurgerySchedule> list = (date != null)
                ? surgeryService.getSurgeriesByDate(date)
                : surgeryService.getAllSurgeries();
        return ResponseEntity.ok(ApiResponse.ok("Surgeries retrieved", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SurgerySchedule>> getSurgeryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Surgery schedule retrieved", surgeryService.getSurgeryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<SurgerySchedule>> scheduleSurgery(@RequestBody SurgerySchedule schedule) {
        SurgerySchedule created = surgeryService.scheduleSurgery(schedule);
        return new ResponseEntity<>(ApiResponse.ok("Surgical procedure scheduled", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/pre-op")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<SurgerySchedule>> updatePreOpChecklist(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        SurgerySchedule updated = surgeryService.updatePreOpChecklist(
                id,
                payload.get("preOpCleared"),
                payload.get("anesthesiaCleared"),
                payload.get("consentSigned"),
                payload.get("bloodReserved")
        );
        return ResponseEntity.ok(ApiResponse.ok("Pre-operative clearance updated", updated));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<SurgerySchedule>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String statusStr = (String) payload.get("status");
        Integer pacuScore = payload.get("pacuRecoveryScore") != null ? ((Number) payload.get("pacuRecoveryScore")).intValue() : null;
        String notes = (String) payload.get("surgicalNotes");
        SurgerySchedule.SurgeryStatus status = SurgerySchedule.SurgeryStatus.valueOf(statusStr);
        SurgerySchedule updated = surgeryService.updateStatus(id, status, pacuScore, notes);
        return ResponseEntity.ok(ApiResponse.ok("Surgery status updated", updated));
    }
}
