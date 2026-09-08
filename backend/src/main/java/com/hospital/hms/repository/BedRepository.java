package com.hospital.hms.repository;

import com.hospital.hms.entity.Bed;
import com.hospital.hms.entity.BedStatus;
import com.hospital.hms.entity.WardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByStatus(BedStatus status);
    List<Bed> findByWardType(WardType wardType);
    Optional<Bed> findByCurrentPatientId(Long patientId);
    long countByStatus(BedStatus status);
}
