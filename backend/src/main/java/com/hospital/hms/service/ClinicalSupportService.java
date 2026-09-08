package com.hospital.hms.service;

import com.hospital.hms.dto.ClinicalSupportDTO;
import java.util.List;

public interface ClinicalSupportService {
    List<ClinicalSupportDTO.SafetyAlert> checkDrugSafety(ClinicalSupportDTO.SafetyCheckRequest request);
    ClinicalSupportDTO.ClinicalBrief generatePatientBrief(Long patientId);
}
