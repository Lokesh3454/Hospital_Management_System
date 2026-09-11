package com.hospital.hms.service;

import com.hospital.hms.dto.AppointmentRequestDTO;
import com.hospital.hms.dto.AppointmentResponseDTO;
import com.hospital.hms.dto.RescheduleRequestDTO;
import com.hospital.hms.dto.TimeSlotDTO;
import com.hospital.hms.entity.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {

    AppointmentResponseDTO bookAppointment(AppointmentRequestDTO request, Long authenticatedUserId);

    AppointmentResponseDTO getAppointmentById(Long id);

    List<AppointmentResponseDTO> getAllAppointments();

    List<AppointmentResponseDTO> getPatientAppointments(Long patientId);

    List<AppointmentResponseDTO> getDoctorAppointments(Long doctorId);

    AppointmentResponseDTO rescheduleAppointment(Long id, RescheduleRequestDTO request);

    AppointmentResponseDTO cancelAppointment(Long id, String reason);

    AppointmentResponseDTO confirmAppointment(Long id);

    AppointmentResponseDTO completeAppointment(Long id);

    List<AppointmentResponseDTO> searchAndFilterAppointments(String search, AppointmentStatus status, LocalDate date, Long doctorId, Long patientId);

    List<AppointmentResponseDTO> searchAndFilterAppointments(String search, AppointmentStatus status, LocalDate date, Long doctorId, Long patientId, Long authenticatedUserId);

    List<TimeSlotDTO> getAvailableTimeSlots(Long doctorId, LocalDate date);

    boolean isDoctorAvailable(Long doctorId, LocalDate date, LocalTime time);

    void seedDiverseSampleAppointments();
}
