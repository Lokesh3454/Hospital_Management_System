package com.hospital.hms.service;

import com.hospital.hms.dto.BedDTO;
import java.util.List;

public interface BedService {
    List<BedDTO> getAllBeds();
    BedDTO getBedById(Long id);
    BedDTO createBed(BedDTO dto);
    BedDTO updateBed(Long id, BedDTO dto);
    BedDTO assignPatientToBed(Long bedId, Long patientId, String notes);
    BedDTO dischargeBed(Long bedId);
    void deleteBed(Long id);
}
