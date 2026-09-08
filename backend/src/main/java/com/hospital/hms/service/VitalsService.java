package com.hospital.hms.service;

import com.hospital.hms.dto.VitalsDTO;
import java.util.List;

public interface VitalsService {
    VitalsDTO recordVitals(VitalsDTO dto);
    List<VitalsDTO> getVitalsByPatient(Long patientId);
    VitalsDTO getLatestVitalsByPatient(Long patientId);
    void deleteVitals(Long id);
}
