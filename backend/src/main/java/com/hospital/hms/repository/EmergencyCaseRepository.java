package com.hospital.hms.repository;

import com.hospital.hms.entity.EmergencyCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyCaseRepository extends JpaRepository<EmergencyCase, Long> {
    List<EmergencyCase> findByStatusNotOrderByCreatedAtDesc(EmergencyCase.EmergencyStatus status);
    List<EmergencyCase> findAllByOrderByCreatedAtDesc();
    long countByStatus(EmergencyCase.EmergencyStatus status);
    long countByTriageLevel(EmergencyCase.TriageLevel triageLevel);
}
