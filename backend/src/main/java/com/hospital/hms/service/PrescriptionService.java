package com.hospital.hms.service;

import com.hospital.hms.dto.PrescriptionRequestDTO;
import com.hospital.hms.dto.PrescriptionResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface PrescriptionService {

    PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO request, Long authenticatedUserId);

    PrescriptionResponseDTO updatePrescription(Long id, PrescriptionRequestDTO request, Long authenticatedUserId);

    PrescriptionResponseDTO getPrescriptionById(Long id, Long authenticatedUserId);

    List<PrescriptionResponseDTO> getPatientPrescriptions(Long patientId, Long authenticatedUserId);

    List<PrescriptionResponseDTO> getDoctorPrescriptions(Long doctorId);

    List<PrescriptionResponseDTO> getAppointmentPrescriptions(Long appointmentId);

    List<PrescriptionResponseDTO> searchAndFilter(String search, LocalDate date, Long patientId, Long doctorId, Long authenticatedUserId);

    void seedDiverseSamplePrescriptions();
}
