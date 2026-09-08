package com.hospital.hms.service.impl;

import com.hospital.hms.entity.EmergencyCase;
import com.hospital.hms.entity.NotificationLog;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.EmergencyCaseRepository;
import com.hospital.hms.repository.NotificationLogRepository;
import com.hospital.hms.service.EmergencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EmergencyServiceImpl implements EmergencyService {

    @Autowired
    private EmergencyCaseRepository emergencyCaseRepository;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyCase> getAllCases() {
        return emergencyCaseRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyCase> getActiveCases() {
        return emergencyCaseRepository.findByStatusNotOrderByCreatedAtDesc(EmergencyCase.EmergencyStatus.DISCHARGED);
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyCase getCaseById(Long id) {
        return emergencyCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency case not found with id " + id));
    }

    @Override
    public EmergencyCase createCase(EmergencyCase emergencyCase) {
        if (emergencyCase.getCaseNumber() == null || emergencyCase.getCaseNumber().isEmpty()) {
            emergencyCase.setCaseNumber("ER-" + System.currentTimeMillis() % 100000);
        }
        if (emergencyCase.getArrivalTime() == null) {
            emergencyCase.setArrivalTime(LocalDateTime.now());
        }
        EmergencyCase saved = emergencyCaseRepository.save(emergencyCase);

        if (Boolean.TRUE.equals(saved.getCodeBlueTriggered()) || saved.getTriageLevel() == EmergencyCase.TriageLevel.RESUSCITATION) {
            logEmergencyBroadcast(saved, "CODE BLUE / RESUSCITATION ALERT: Immediate trauma team response required in Bay " + (saved.getAssignedBedNumber() != null ? saved.getAssignedBedNumber() : "ER Trauma Area"));
        }
        return saved;
    }

    @Override
    public EmergencyCase updateTriage(Long id, EmergencyCase.TriageLevel level, String notes) {
        EmergencyCase ec = getCaseById(id);
        ec.setTriageLevel(level);
        if (notes != null && !notes.trim().isEmpty()) {
            ec.setChiefComplaint(ec.getChiefComplaint() + " | Triage Update: " + notes);
        }
        return emergencyCaseRepository.save(ec);
    }

    @Override
    public EmergencyCase updateStatus(Long id, EmergencyCase.EmergencyStatus status, String bedNumber) {
        EmergencyCase ec = getCaseById(id);
        ec.setStatus(status);
        if (bedNumber != null && !bedNumber.isEmpty()) {
            ec.setAssignedBedNumber(bedNumber);
        }
        return emergencyCaseRepository.save(ec);
    }

    @Override
    public EmergencyCase triggerCodeBlue(Long id) {
        EmergencyCase ec = getCaseById(id);
        ec.setCodeBlueTriggered(true);
        ec.setTriageLevel(EmergencyCase.TriageLevel.RESUSCITATION);
        ec.setStatus(EmergencyCase.EmergencyStatus.IN_TREATMENT);
        EmergencyCase saved = emergencyCaseRepository.save(ec);

        logEmergencyBroadcast(saved, "CODE BLUE ACTIVATED for Patient " + saved.getPatientName() + " (" + saved.getCaseNumber() + "). All available CPR and ICU personnel report immediately.");
        return saved;
    }

    private void logEmergencyBroadcast(EmergencyCase ec, String message) {
        NotificationLog log = new NotificationLog();
        log.setRecipientName("Emergency Trauma & ICU Team");
        log.setRecipientContact("hospital-emergency-pager@hospital.internal");
        log.setChannel(NotificationLog.NotificationChannel.BROADCAST_ALARM);
        log.setTriggerEvent("CODE_BLUE_ALERT");
        log.setSubject("🚨 CRITICAL CODE BLUE BROADCAST: " + ec.getCaseNumber());
        log.setMessage(message);
        log.setStatus(NotificationLog.DeliveryStatus.DELIVERED);
        notificationLogRepository.save(log);
    }
}
