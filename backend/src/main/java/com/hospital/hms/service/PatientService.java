package com.hospital.hms.service;

import com.hospital.hms.dto.PatientDTO;
import java.util.List;

public interface PatientService {

    List<PatientDTO> getAllPatients(String search, String gender, String bloodGroup);

    List<PatientDTO> searchAndFilter(String search, String name, String phone, String email, String gender, String bloodGroup);

    List<PatientDTO> searchAndFilter(String search, String name, String phone, String email, String gender, String bloodGroup, Long doctorId);

    PatientDTO getPatientById(Long id);

    PatientDTO addPatient(PatientDTO patientDTO);

    PatientDTO updatePatient(Long id, PatientDTO patientDTO);

    void deletePatient(Long id);
}
