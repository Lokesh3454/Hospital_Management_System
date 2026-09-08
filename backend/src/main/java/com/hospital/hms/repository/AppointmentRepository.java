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

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user pu JOIN FETCH a.doctor d JOIN FETCH d.user du WHERE p.id = :patientId ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(@Param("patientId") Long patientId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user pu JOIN FETCH a.doctor d JOIN FETCH d.user du WHERE d.id = :doctorId ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(@Param("doctorId") Long doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user pu JOIN FETCH a.doctor d JOIN FETCH d.user du WHERE d.id = :doctorId AND a.appointmentDate = :appointmentDate ORDER BY a.appointmentTime ASC")
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(@Param("doctorId") Long doctorId, @Param("appointmentDate") LocalDate appointmentDate);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user pu JOIN FETCH a.doctor d JOIN FETCH d.user du WHERE a.appointmentDate = :appointmentDate ORDER BY a.appointmentTime ASC")
    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(@Param("appointmentDate") LocalDate appointmentDate);

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

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user pu JOIN FETCH a.doctor d JOIN FETCH d.user du WHERE a.doctor.id = :doctorId AND a.appointmentDate >= :date AND a.status NOT IN ('CANCELLED', 'COMPLETED') ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findUpcomingByDoctorId(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    List<Appointment> findByPatientIdAndAppointmentDateGreaterThanEqualOrderByAppointmentDateAscAppointmentTimeAsc(Long patientId, LocalDate appointmentDate);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user pu JOIN FETCH a.doctor d JOIN FETCH d.user du WHERE a.patient.id = :patientId AND a.appointmentDate >= :date AND a.status NOT IN ('CANCELLED', 'COMPLETED') ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findUpcomingByPatientId(@Param("patientId") Long patientId, @Param("date") LocalDate date);

    List<Appointment> findTop5ByOrderByAppointmentDateDescAppointmentTimeDesc();

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor.id = :doctorId")
    long countDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotAndIdNot(
            Long doctorId, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status, Long id);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user pu JOIN FETCH a.doctor d JOIN FETCH d.user du WHERE " +
           "(:patientId IS NULL OR p.id = :patientId) AND " +
           "(:doctorId IS NULL OR d.id = :doctorId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:date IS NULL OR a.appointmentDate = :date) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(pu.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pu.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(pu.firstName, ' ', pu.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(du.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(du.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(du.firstName, ' ', du.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
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
