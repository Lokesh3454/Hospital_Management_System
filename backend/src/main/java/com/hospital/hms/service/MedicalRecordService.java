package com.hospital.hms.service;

import com.hospital.hms.dto.MedicalRecordRequestDTO;
import com.hospital.hms.dto.MedicalRecordResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface MedicalRecordService {

    MedicalRecordResponseDTO createRecord(MedicalRecordRequestDTO request, Long authenticatedUserId);

    MedicalRecordResponseDTO updateRecord(Long id, MedicalRecordRequestDTO request, Long authenticatedUserId);

    MedicalRecordResponseDTO getRecordById(Long id, Long authenticatedUserId);

    List<MedicalRecordResponseDTO> getPatientHistory(Long patientId, Long authenticatedUserId);

    List<MedicalRecordResponseDTO> getDoctorRecords(Long doctorId);

    List<MedicalRecordResponseDTO> searchAndFilter(String search, LocalDate date, Long patientId, Long doctorId, Long authenticatedUserId);

    void deleteRecord(Long id, Long authenticatedUserId);

    void deleteAllRecords();

    void seedDiverseSampleRecords();
}

