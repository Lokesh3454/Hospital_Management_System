package com.hospital.hms.service;

import com.hospital.hms.entity.StaffShift;
import java.util.List;

public interface StaffRosterService {
    List<StaffShift> getAllShifts();
    List<StaffShift> getShiftsByDay(String dayOfWeek);
    List<StaffShift> getOnCallSpecialists();
    StaffShift assignShift(StaffShift shift);
    StaffShift toggleOnCall(Long id, boolean onCall);
    void deleteShift(Long id);
}
