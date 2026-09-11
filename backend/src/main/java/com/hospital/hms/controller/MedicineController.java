package com.hospital.hms.controller;

import com.hospital.hms.dto.MedicineDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<MedicineDTO>>> getAllMedicines(
            @RequestParam(required = false) String search) {
        List<MedicineDTO> list;
        if (search != null && !search.trim().isEmpty()) {
            list = medicineService.searchMedicines(search);
        } else {
            list = medicineService.getAllMedicines();
        }
        return ResponseEntity.ok(ApiResponse.ok("Medicines retrieved successfully", list));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<MedicineDTO>>> getLowStockMedicines() {
        List<MedicineDTO> list = medicineService.getLowStockMedicines();
        return ResponseEntity.ok(ApiResponse.ok("Low stock medicines retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<MedicineDTO>> getMedicineById(@PathVariable Long id) {
        MedicineDTO dto = medicineService.getMedicineById(id);
        return ResponseEntity.ok(ApiResponse.ok("Medicine retrieved successfully", dto));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<MedicineDTO>> createMedicine(@RequestBody MedicineDTO dto) {
        MedicineDTO created = medicineService.createMedicine(dto);
        return new ResponseEntity<>(ApiResponse.ok("Medicine added to inventory successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<MedicineDTO>> updateMedicine(
            @PathVariable Long id,
            @RequestBody MedicineDTO dto) {
        MedicineDTO updated = medicineService.updateMedicine(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Medicine updated successfully", updated));
    }

    @PostMapping("/{id}/adjust-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<MedicineDTO>> adjustStock(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("delta") || payload.get("delta") == null) {
            throw new com.hospital.hms.exception.BadRequestException("delta value is required to adjust medicine stock.");
        }
        Integer delta;
        try {
            delta = Integer.valueOf(payload.get("delta").toString());
        } catch (NumberFormatException e) {
            throw new com.hospital.hms.exception.BadRequestException("Invalid delta: must be an integer.");
        }
        MedicineDTO updated = medicineService.adjustStock(id, delta);
        return ResponseEntity.ok(ApiResponse.ok("Medicine stock updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.ok("Medicine removed from inventory successfully", null));
    }
}
