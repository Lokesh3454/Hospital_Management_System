package com.hospital.hms.controller;

import com.hospital.hms.dto.AppointmentRequestDTO;
import com.hospital.hms.dto.AppointmentResponseDTO;
import com.hospital.hms.dto.RescheduleRequestDTO;
import com.hospital.hms.dto.TimeSlotDTO;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.entity.AppointmentStatus;
import com.hospital.hms.security.services.UserDetailsImpl;
import com.hospital.hms.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> bookAppointment(
            @Valid @RequestBody AppointmentRequestDTO request) {
        Long authenticatedUserId = getAuthenticatedUserId();
        AppointmentResponseDTO booked = appointmentService.bookAppointment(request, authenticatedUserId);
        return new ResponseEntity<>(ApiResponse.ok("Appointment booked successfully", booked), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getAppointments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId) {
        List<AppointmentResponseDTO> appointments = appointmentService.searchAndFilterAppointments(search, status, date, doctorId, patientId);
        return ResponseEntity.ok(ApiResponse.ok("Appointments retrieved successfully", appointments));
    }

    @Autowired
    private com.hospital.hms.repository.PatientRepository patientRepository;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> getAppointmentById(@PathVariable Long id) {
        AppointmentResponseDTO appointment = appointmentService.getAppointmentById(id);
        enforcePatientPrivacy(appointment.getPatientId());
        return ResponseEntity.ok(ApiResponse.ok("Appointment retrieved successfully", appointment));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getPatientAppointments(@PathVariable Long patientId) {
        enforcePatientPrivacy(patientId);
        List<AppointmentResponseDTO> list = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(ApiResponse.ok("Patient appointments retrieved", list));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getDoctorAppointments(@PathVariable Long doctorId) {
        List<AppointmentResponseDTO> list = appointmentService.getDoctorAppointments(doctorId);
        return ResponseEntity.ok(ApiResponse.ok("Doctor appointments retrieved", list));
    }

    @GetMapping("/available-slots")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TimeSlotDTO> slots = appointmentService.getAvailableTimeSlots(doctorId, date);
        return ResponseEntity.ok(ApiResponse.ok("Available time slots retrieved", slots));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequestDTO request) {
        AppointmentResponseDTO rescheduled = appointmentService.rescheduleAppointment(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Appointment rescheduled successfully", rescheduled));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload) {
        String reason = payload != null ? payload.get("reason") : null;
        AppointmentResponseDTO cancelled = appointmentService.cancelAppointment(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Appointment cancelled successfully", cancelled));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> confirmAppointment(@PathVariable Long id) {
        AppointmentResponseDTO confirmed = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(ApiResponse.ok("Appointment confirmed successfully", confirmed));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> completeAppointment(@PathVariable Long id) {
        AppointmentResponseDTO completed = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(ApiResponse.ok("Appointment marked as completed", completed));
    }

    @PostMapping("/seed")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<String>> seedAppointments() {
        appointmentService.seedDiverseSampleAppointments();
        return ResponseEntity.ok(ApiResponse.ok("Sample appointments seeded successfully", "Clean upcoming and clinical appointments initialized."));
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        return null;
    }

    private void enforcePatientPrivacy(Long requestedPatientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            boolean isStaff = auth.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_ADMIN") ||
                    a.getAuthority().equals("ROLE_DOCTOR") ||
                    a.getAuthority().equals("ROLE_RECEPTIONIST"));
            if (!isStaff) {
                com.hospital.hms.entity.Patient currentPatient = patientRepository.findByUserId(userDetails.getId())
                        .orElseThrow(() -> new com.hospital.hms.exception.BadRequestException("No patient profile found for this account."));
                if (!currentPatient.getId().equals(requestedPatientId)) {
                    throw new com.hospital.hms.exception.BadRequestException("Access denied: You can only view your own appointments.");
                }
            }
        }
    }
}
