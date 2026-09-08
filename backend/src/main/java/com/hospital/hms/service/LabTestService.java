package com.hospital.hms.service;

import com.hospital.hms.dto.LabTestDTO;
import java.util.List;

public interface LabTestService {
    List<LabTestDTO> getAllLabTests();
    List<LabTestDTO> getLabTestsByPatient(Long patientId);
    List<LabTestDTO> getLabTestsByDoctor(Long doctorId);
    LabTestDTO getLabTestById(Long id);
    LabTestDTO createLabTest(LabTestDTO dto);
    LabTestDTO updateLabTestStatus(Long id, String status);
    LabTestDTO recordResults(Long id, String resultValue, String interpretation, String remarks);
    void deleteLabTest(Long id);
}
