package com.hospital.hms.service.impl;

import com.hospital.hms.entity.SurgerySchedule;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.SurgeryScheduleRepository;
import com.hospital.hms.service.SurgeryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class SurgeryServiceImpl implements SurgeryService {

    @Autowired
    private SurgeryScheduleRepository surgeryScheduleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SurgerySchedule> getAllSurgeries() {
        return surgeryScheduleRepository.findAllByOrderBySurgeryDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurgerySchedule> getSurgeriesByDate(LocalDate date) {
        return surgeryScheduleRepository.findBySurgeryDateOrderByScheduledStartTimeAsc(date);
    }

    @Override
    @Transactional(readOnly = true)
    public SurgerySchedule getSurgeryById(Long id) {
        return surgeryScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surgery schedule not found with id " + id));
    }

    @Override
    public SurgerySchedule scheduleSurgery(SurgerySchedule schedule) {
        if (schedule.getSurgeryNumber() == null || schedule.getSurgeryNumber().isEmpty()) {
            schedule.setSurgeryNumber("SURG-" + System.currentTimeMillis() % 100000);
        }
        if (schedule.getSurgeryDate() == null) {
            schedule.setSurgeryDate(LocalDate.now());
        }
        return surgeryScheduleRepository.save(schedule);
    }

    @Override
    public SurgerySchedule updatePreOpChecklist(Long id, Boolean preOp, Boolean anesthesia, Boolean consent, Boolean blood) {
        SurgerySchedule schedule = getSurgeryById(id);
        if (preOp != null) schedule.setPreOpCleared(preOp);
        if (anesthesia != null) schedule.setAnesthesiaCleared(anesthesia);
        if (consent != null) schedule.setConsentSigned(consent);
        if (blood != null) schedule.setBloodReserved(blood);
        return surgeryScheduleRepository.save(schedule);
    }

    @Override
    public SurgerySchedule updateStatus(Long id, SurgerySchedule.SurgeryStatus status, Integer pacuScore, String notes) {
        SurgerySchedule schedule = getSurgeryById(id);
        schedule.setStatus(status);
        if (pacuScore != null) {
            schedule.setPacuRecoveryScore(pacuScore);
        }
        if (notes != null && !notes.trim().isEmpty()) {
            String current = schedule.getSurgicalNotes() != null ? schedule.getSurgicalNotes() + " | " : "";
            schedule.setSurgicalNotes(current + notes);
        }
        return surgeryScheduleRepository.save(schedule);
    }
}
