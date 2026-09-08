package com.hospital.hms.repository;

import com.hospital.hms.entity.Doctor;
import com.hospital.hms.entity.DoctorStatus;
import com.hospital.hms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUser(User user);

    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findBySpecializationIgnoreCase(String specialization);

    List<Doctor> findByDepartmentIgnoreCase(String department);

    @Query("SELECT DISTINCT d.specialization FROM Doctor d WHERE d.specialization IS NOT NULL ORDER BY d.specialization")
    List<String> findDistinctSpecializations();

    @Query("SELECT d FROM Doctor d JOIN FETCH d.user u WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:specialization IS NULL OR :specialization = '' OR LOWER(d.specialization) = LOWER(:specialization)) AND " +
           "(:status IS NULL OR d.status = :status) " +
           "ORDER BY d.id DESC")
    List<Doctor> searchAndFilter(
            @Param("search") String search,
            @Param("specialization") String specialization,
            @Param("status") DoctorStatus status);

    @Query("SELECT d FROM Doctor d JOIN FETCH d.user u WHERE " +
           "(:name IS NULL OR :name = '' OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:specialization IS NULL OR :specialization = '' OR LOWER(d.specialization) = LOWER(:specialization)) AND " +
           "(:minExperience IS NULL OR d.experienceYears >= :minExperience) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY d.id DESC")
    List<Doctor> searchAndFilterAdvanced(
            @Param("search") String search,
            @Param("name") String name,
            @Param("specialization") String specialization,
            @Param("minExperience") Integer minExperience,
            @Param("status") DoctorStatus status);
}
