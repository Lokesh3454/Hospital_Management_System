package com.hospital.hms.repository;

import com.hospital.hms.entity.StaffShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffShiftRepository extends JpaRepository<StaffShift, Long> {
    List<StaffShift> findByDayOfWeek(String dayOfWeek);
    List<StaffShift> findByDepartment(String department);
    List<StaffShift> findByOnCallTrue();
}
