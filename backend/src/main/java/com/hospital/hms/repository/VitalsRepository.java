package com.hospital.hms.repository;

import com.hospital.hms.entity.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, Long> {
    List<Vitals> findByPatientIdOrderByRecordedAtDesc(Long patientId);
    Optional<Vitals> findTopByPatientIdOrderByRecordedAtDesc(Long patientId);
    List<Vitals> findByPatientId(Long patientId);
}
