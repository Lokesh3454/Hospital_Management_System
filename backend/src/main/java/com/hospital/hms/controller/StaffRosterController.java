package com.hospital.hms.controller;

import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.entity.StaffShift;
import com.hospital.hms.service.StaffRosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/staff-roster")
public class StaffRosterController {

    @Autowired
    private StaffRosterService staffRosterService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<StaffShift>>> getAllShifts(
            @RequestParam(required = false) String dayOfWeek) {
        List<StaffShift> list = (dayOfWeek != null && !dayOfWeek.isEmpty())
                ? staffRosterService.getShiftsByDay(dayOfWeek)
                : staffRosterService.getAllShifts();
        return ResponseEntity.ok(ApiResponse.ok("Staff shifts retrieved", list));
    }

    @GetMapping("/on-call")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<StaffShift>>> getOnCallSpecialists() {
        return ResponseEntity.ok(ApiResponse.ok("On-call specialists retrieved", staffRosterService.getOnCallSpecialists()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<StaffShift>> assignShift(@RequestBody StaffShift shift) {
        StaffShift created = staffRosterService.assignShift(shift);
        return new ResponseEntity<>(ApiResponse.ok("Staff shift rostered successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/on-call")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<StaffShift>> toggleOnCall(
            @PathVariable Long id,
            @RequestParam boolean onCall) {
        StaffShift updated = staffRosterService.toggleOnCall(id, onCall);
        return ResponseEntity.ok(ApiResponse.ok("On-call status updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteShift(@PathVariable Long id) {
        staffRosterService.deleteShift(id);
        return ResponseEntity.ok(ApiResponse.ok("Shift deleted", null));
    }
}
