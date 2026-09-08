package com.hospital.hms.repository;

import com.hospital.hms.entity.Patient;
import com.hospital.hms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUser(User user);

    Optional<Patient> findByUserId(Long userId);

    @Query("SELECT p FROM Patient p JOIN FETCH p.user u WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "u.phone LIKE CONCAT('%', :search, '%')) AND " +
           "(:gender IS NULL OR :gender = '' OR LOWER(p.gender) = LOWER(:gender)) AND " +
           "(:bloodGroup IS NULL OR :bloodGroup = '' OR p.bloodGroup = :bloodGroup) " +
           "ORDER BY p.id DESC")
    List<Patient> searchAndFilter(
            @Param("search") String search,
            @Param("gender") String gender,
            @Param("bloodGroup") String bloodGroup);

    @Query("SELECT DISTINCT p FROM Patient p JOIN FETCH p.user u WHERE " +
           "(:doctorId IS NULL OR " +
           " EXISTS (SELECT 1 FROM Appointment a WHERE a.patient.id = p.id AND a.doctor.id = :doctorId) OR " +
           " EXISTS (SELECT 1 FROM MedicalRecord m WHERE m.patient.id = p.id AND m.doctor.id = :doctorId) OR " +
           " EXISTS (SELECT 1 FROM Prescription pr WHERE pr.patient.id = p.id AND pr.doctor.id = :doctorId)) AND " +
           "(:name IS NULL OR :name = '' OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:phone IS NULL OR :phone = '' OR u.phone LIKE CONCAT('%', :phone, '%')) AND " +
           "(:email IS NULL OR :email = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:gender IS NULL OR :gender = '' OR LOWER(p.gender) = LOWER(:gender)) AND " +
           "(:bloodGroup IS NULL OR :bloodGroup = '' OR p.bloodGroup = :bloodGroup) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "u.phone LIKE CONCAT('%', :search, '%')) " +
           "ORDER BY p.id DESC")
    List<Patient> searchAndFilterAdvanced(
            @Param("search") String search,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("gender") String gender,
            @Param("bloodGroup") String bloodGroup,
            @Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(DISTINCT p.id) FROM Patient p WHERE " +
           "EXISTS (SELECT 1 FROM Appointment a WHERE a.patient.id = p.id AND a.doctor.id = :doctorId) OR " +
           "EXISTS (SELECT 1 FROM MedicalRecord m WHERE m.patient.id = p.id AND m.doctor.id = :doctorId) OR " +
           "EXISTS (SELECT 1 FROM Prescription pr WHERE pr.patient.id = p.id AND pr.doctor.id = :doctorId)")
    long countDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);
}
