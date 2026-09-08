package com.hospital.hms.repository;

import com.hospital.hms.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientIdOrderByPrescriptionDateDescCreatedAtDesc(Long patientId);

    List<Prescription> findByDoctorIdOrderByPrescriptionDateDescCreatedAtDesc(Long doctorId);

    List<Prescription> findByAppointmentId(Long appointmentId);

    long countByDoctorId(Long doctorId);

    long countByPatientId(Long patientId);

    @Query("SELECT DISTINCT p FROM Prescription p LEFT JOIN p.items item WHERE " +
           "(:patientId IS NULL OR p.patient.id = :patientId) AND " +
           "(:doctorId IS NULL OR p.doctor.id = :doctorId) AND " +
           "(:date IS NULL OR p.prescriptionDate = :date) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.patient.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.patient.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(p.patient.user.firstName, ' ', p.patient.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.doctor.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.doctor.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(item.medicineName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.notes) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY p.prescriptionDate DESC, p.createdAt DESC")
    List<Prescription> searchAndFilter(
            @Param("search") String search,
            @Param("date") LocalDate date,
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId);
}
