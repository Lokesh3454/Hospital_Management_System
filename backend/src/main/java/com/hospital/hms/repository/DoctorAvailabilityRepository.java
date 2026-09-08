package com.hospital.hms.repository;

import com.hospital.hms.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorId(Long doctorId);

    List<DoctorAvailability> findByDoctorIdAndAvailableTrue(Long doctorId);

    Optional<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, String dayOfWeek);

    void deleteByDoctorId(Long doctorId);
}
