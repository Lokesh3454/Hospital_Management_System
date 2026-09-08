package com.hospital.hms.repository;

import com.hospital.hms.entity.SurgerySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SurgeryScheduleRepository extends JpaRepository<SurgerySchedule, Long> {
    List<SurgerySchedule> findBySurgeryDateOrderByScheduledStartTimeAsc(LocalDate date);
    List<SurgerySchedule> findAllByOrderBySurgeryDateDesc();
    long countByStatus(SurgerySchedule.SurgeryStatus status);
}
