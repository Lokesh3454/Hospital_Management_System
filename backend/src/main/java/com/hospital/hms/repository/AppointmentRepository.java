package com.hospital.hms.repository;

import com.hospital.hms.entity.Appointment;
import com.hospital.hms.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(Long doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(Long doctorId, LocalDate appointmentDate);

    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(LocalDate appointmentDate);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(Long doctorId, LocalDate appointmentDate, AppointmentStatus status);

    List<Appointment> findByStatus(AppointmentStatus status);

    long countByAppointmentDate(LocalDate appointmentDate);

    long countByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

    long countByDoctorIdAndAppointmentDateGreaterThanEqual(Long doctorId, LocalDate appointmentDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate >= :date AND a.status NOT IN ('CANCELLED', 'COMPLETED')")
    long countUpcomingByDoctorId(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    long countByPatientId(Long patientId);

    long countByPatientIdAndAppointmentDateGreaterThanEqual(Long patientId, LocalDate appointmentDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= :date AND a.status NOT IN ('CANCELLED', 'COMPLETED')")
    long countUpcomingByPatientId(@Param("patientId") Long patientId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId AND (a.appointmentDate < :date OR a.status IN ('COMPLETED', 'CANCELLED'))")
    long countHistoryByPatientId(@Param("patientId") Long patientId, @Param("date") LocalDate date);

    List<Appointment> findByDoctorIdAndAppointmentDateGreaterThanEqualOrderByAppointmentDateAscAppointmentTimeAsc(Long doctorId, LocalDate appointmentDate);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate >= :date AND a.status NOT IN ('CANCELLED', 'COMPLETED') ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findUpcomingByDoctorId(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    List<Appointment> findByPatientIdAndAppointmentDateGreaterThanEqualOrderByAppointmentDateAscAppointmentTimeAsc(Long patientId, LocalDate appointmentDate);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= :date AND a.status NOT IN ('CANCELLED', 'COMPLETED') ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findUpcomingByPatientId(@Param("patientId") Long patientId, @Param("date") LocalDate date);

    List<Appointment> findTop5ByOrderByAppointmentDateDescAppointmentTimeDesc();

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor.id = :doctorId")
    long countDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotAndIdNot(
            Long doctorId, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status, Long id);

    @Query("SELECT a FROM Appointment a WHERE " +
           "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
           "(:doctorId IS NULL OR a.doctor.id = :doctorId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:date IS NULL OR a.appointmentDate = :date) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.patient.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.patient.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(a.patient.user.firstName, ' ', a.patient.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.doctor.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.doctor.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(a.doctor.user.firstName, ' ', a.doctor.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.doctor.specialization) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.reason) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.notes) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> searchAndFilter(
            @Param("search") String search,
            @Param("status") AppointmentStatus status,
            @Param("date") LocalDate date,
            @Param("doctorId") Long doctorId,
            @Param("patientId") Long patientId);
}
