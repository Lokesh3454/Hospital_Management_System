package com.hospital.hms.repository;

import com.hospital.hms.entity.LabTest;
import com.hospital.hms.entity.LabTestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    List<LabTest> findByPatientIdOrderByOrderDateDesc(Long patientId);
    List<LabTest> findByDoctorIdOrderByOrderDateDesc(Long doctorId);
    List<LabTest> findByStatus(LabTestStatus status);
    List<LabTest> findByPatientId(Long patientId);
}
