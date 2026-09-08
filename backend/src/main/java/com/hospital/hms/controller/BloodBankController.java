package com.hospital.hms.controller;

import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.entity.BloodInventory;
import com.hospital.hms.service.BloodBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/blood-bank")
public class BloodBankController {

    @Autowired
    private BloodBankService bloodBankService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BloodInventory>>> getAllInventory() {
        return ResponseEntity.ok(ApiResponse.ok("Blood bank inventory units retrieved", bloodBankService.getAllInventory()));
    }

    @GetMapping("/group/{bloodGroup}")
    public ResponseEntity<ApiResponse<List<BloodInventory>>> getByGroup(@PathVariable String bloodGroup) {
        return ResponseEntity.ok(ApiResponse.ok("Blood units retrieved for group " + bloodGroup, bloodBankService.getInventoryByGroup(bloodGroup)));
    }

    @PostMapping("/restock")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BloodInventory>> restockUnits(@RequestBody Map<String, Object> payload) {
        String bloodGroup = (String) payload.get("bloodGroup");
        String compTypeStr = (String) payload.get("componentType");
        int units = ((Number) payload.get("units")).intValue();
        BloodInventory.ComponentType compType = compTypeStr != null
                ? BloodInventory.ComponentType.valueOf(compTypeStr)
                : BloodInventory.ComponentType.WHOLE_BLOOD;

        BloodInventory updated = bloodBankService.restockUnits(bloodGroup, compType, units);
        return ResponseEntity.ok(ApiResponse.ok("Blood units stocked successfully", updated));
    }

    @PostMapping("/{id}/reserve")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BloodInventory>> reserveUnits(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Integer> payload) {
        int units = (payload != null && payload.containsKey("units")) ? payload.get("units") : 1;
        BloodInventory updated = bloodBankService.reserveUnits(id, units);
        return ResponseEntity.ok(ApiResponse.ok("Blood units reserved for patient/surgery", updated));
    }

    @PutMapping("/{id}/reserve")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BloodInventory>> reserveUnitsPut(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int units) {
        BloodInventory updated = bloodBankService.reserveUnits(id, units);
        return ResponseEntity.ok(ApiResponse.ok("Blood units reserved for patient/surgery", updated));
    }

    @PutMapping("/{id}/restock")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BloodInventory>> restockUnitsPut(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int units) {
        BloodInventory updated = bloodBankService.restockUnits(id, units);
        return ResponseEntity.ok(ApiResponse.ok("Blood units restocked successfully", updated));
    }

    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BloodInventory>> issueUnits(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload) {
        int units = payload.getOrDefault("units", 1);
        BloodInventory updated = bloodBankService.issueUnits(id, units);
        return ResponseEntity.ok(ApiResponse.ok("Blood units issued for clinical transfusion", updated));
    }
}
