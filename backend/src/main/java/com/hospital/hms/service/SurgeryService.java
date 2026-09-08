package com.hospital.hms.service;

import com.hospital.hms.entity.SurgerySchedule;
import java.time.LocalDate;
import java.util.List;

public interface SurgeryService {
    List<SurgerySchedule> getAllSurgeries();
    List<SurgerySchedule> getSurgeriesByDate(LocalDate date);
    SurgerySchedule getSurgeryById(Long id);
    SurgerySchedule scheduleSurgery(SurgerySchedule schedule);
    SurgerySchedule updatePreOpChecklist(Long id, Boolean preOp, Boolean anesthesia, Boolean consent, Boolean blood);
    SurgerySchedule updateStatus(Long id, SurgerySchedule.SurgeryStatus status, Integer pacuScore, String notes);
}
