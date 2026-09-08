package com.hospital.hms.repository;

import com.hospital.hms.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientIdOrderByRecordDateDescCreatedAtDesc(Long patientId);

    List<MedicalRecord> findByDoctorIdOrderByRecordDateDescCreatedAtDesc(Long doctorId);

    List<MedicalRecord> findByAppointmentId(Long appointmentId);

    long countByDoctorId(Long doctorId);

    long countByPatientId(Long patientId);

    List<MedicalRecord> findTop5ByDoctorIdOrderByRecordDateDescCreatedAtDesc(Long doctorId);

    @Query("SELECT r FROM MedicalRecord r WHERE " +
           "(:patientId IS NULL OR r.patient.id = :patientId) AND " +
           "(:doctorId IS NULL OR r.doctor.id = :doctorId) AND " +
           "(:date IS NULL OR r.recordDate = :date) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.patient.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.patient.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(r.patient.user.firstName, ' ', r.patient.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.doctor.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.doctor.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.diagnosis) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.symptoms) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.treatmentPlan) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.testResults) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.notes) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.recordDate DESC, r.createdAt DESC")
    List<MedicalRecord> searchAndFilter(
            @Param("search") String search,
            @Param("date") LocalDate date,
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId);
}
