package com.hospital.hms.controller;

import com.hospital.hms.dto.BedDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/beds")
public class BedController {

    @Autowired
    private BedService bedService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<BedDTO>>> getAllBeds() {
        List<BedDTO> beds = bedService.getAllBeds();
        return ResponseEntity.ok(ApiResponse.ok("Beds retrieved successfully", beds));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BedDTO>> getBedById(@PathVariable Long id) {
        BedDTO bed = bedService.getBedById(id);
        return ResponseEntity.ok(ApiResponse.ok("Bed retrieved successfully", bed));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<BedDTO>> createBed(@RequestBody BedDTO dto) {
        BedDTO created = bedService.createBed(dto);
        return new ResponseEntity<>(ApiResponse.ok("Bed created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BedDTO>> updateBed(@PathVariable Long id, @RequestBody BedDTO dto) {
        BedDTO updated = bedService.updateBed(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Bed updated successfully", updated));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BedDTO>> assignPatient(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("patientId") || payload.get("patientId") == null) {
            throw new com.hospital.hms.exception.BadRequestException("patientId is required to admit a patient to a bed.");
        }
        Long patientId;
        try {
            patientId = Long.valueOf(payload.get("patientId").toString());
        } catch (NumberFormatException e) {
            throw new com.hospital.hms.exception.BadRequestException("Invalid patientId: must be a numeric ID.");
        }
        String notes = payload.containsKey("notes") && payload.get("notes") != null ? payload.get("notes").toString() : "";
        BedDTO bed = bedService.assignPatientToBed(id, patientId, notes);
        return ResponseEntity.ok(ApiResponse.ok("Patient admitted to bed successfully", bed));
    }

    @PostMapping("/{id}/discharge")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BedDTO>> dischargePatient(@PathVariable Long id) {
        BedDTO bed = bedService.dischargeBed(id);
        return ResponseEntity.ok(ApiResponse.ok("Bed vacated and marked for cleaning", bed));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBed(@PathVariable Long id) {
        bedService.deleteBed(id);
        return ResponseEntity.ok(ApiResponse.ok("Bed deleted successfully", null));
    }
}
