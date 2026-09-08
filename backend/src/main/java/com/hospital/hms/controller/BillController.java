package com.hospital.hms.controller;

import com.hospital.hms.dto.BillRequestDTO;
import com.hospital.hms.dto.BillResponseDTO;
import com.hospital.hms.dto.PaymentUpdateDTO;
import com.hospital.hms.entity.PaymentStatus;
import com.hospital.hms.security.services.UserDetailsImpl;
import com.hospital.hms.service.BillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BillController {

    @Autowired
    private BillService billService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BillResponseDTO> createBill(
            @Valid @RequestBody BillRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long authUserId = userDetails != null ? userDetails.getId() : null;
        BillResponseDTO response = billService.createBill(request, authUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BillResponseDTO> updateBill(
            @PathVariable Long id,
            @Valid @RequestBody BillRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long authUserId = userDetails != null ? userDetails.getId() : null;
        BillResponseDTO response = billService.updateBill(id, request, authUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<BillResponseDTO> getBillById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long authUserId = userDetails != null ? userDetails.getId() : null;
        BillResponseDTO response = billService.getBillById(id, authUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST') or hasRole('PATIENT')")
    public ResponseEntity<List<BillResponseDTO>> getPatientBills(
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long authUserId = userDetails != null ? userDetails.getId() : null;
        List<BillResponseDTO> response = billService.getPatientBills(patientId, authUserId);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{id}/pay", method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST') or hasRole('PATIENT')")
    public ResponseEntity<BillResponseDTO> markBillAsPaid(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpdateDTO paymentDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long authUserId = userDetails != null ? userDetails.getId() : null;
        BillResponseDTO response = billService.markBillAsPaid(id, paymentDTO, authUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST') or hasRole('PATIENT')")
    public ResponseEntity<List<BillResponseDTO>> searchAndFilterBills(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long authUserId = userDetails != null ? userDetails.getId() : null;
        List<BillResponseDTO> response = billService.searchAndFilterBills(search, patientId, date, paymentStatus, authUserId);
        return ResponseEntity.ok(response);
    }
}
