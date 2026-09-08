package com.hospital.hms.service;

import com.hospital.hms.dto.DoctorDTO;
import com.hospital.hms.entity.DoctorStatus;
import java.util.List;

public interface DoctorService {

    List<DoctorDTO> getAllDoctors(String search, String specialization, DoctorStatus status);

    List<DoctorDTO> searchAndFilter(String search, String name, String specialization, Integer minExperience, DoctorStatus status);

    List<String> getAllSpecializations();

    DoctorDTO getDoctorById(Long id);

    DoctorDTO addDoctor(DoctorDTO doctorDTO);

    DoctorDTO updateDoctor(Long id, DoctorDTO doctorDTO);

    void deleteDoctor(Long id);
}
