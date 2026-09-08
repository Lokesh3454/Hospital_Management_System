package com.hospital.hms.service;

import com.hospital.hms.entity.EmergencyCase;
import java.util.List;

public interface EmergencyService {
    List<EmergencyCase> getAllCases();
    List<EmergencyCase> getActiveCases();
    EmergencyCase getCaseById(Long id);
    EmergencyCase createCase(EmergencyCase emergencyCase);
    EmergencyCase updateTriage(Long id, EmergencyCase.TriageLevel level, String notes);
    EmergencyCase updateStatus(Long id, EmergencyCase.EmergencyStatus status, String bedNumber);
    EmergencyCase triggerCodeBlue(Long id);
}
