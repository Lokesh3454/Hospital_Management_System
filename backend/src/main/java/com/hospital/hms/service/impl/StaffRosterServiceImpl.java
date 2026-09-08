package com.hospital.hms.service.impl;

import com.hospital.hms.entity.StaffShift;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.StaffShiftRepository;
import com.hospital.hms.service.StaffRosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StaffRosterServiceImpl implements StaffRosterService {

    @Autowired
    private StaffShiftRepository staffShiftRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StaffShift> getAllShifts() {
        return staffShiftRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffShift> getShiftsByDay(String dayOfWeek) {
        return staffShiftRepository.findByDayOfWeek(dayOfWeek);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffShift> getOnCallSpecialists() {
        return staffShiftRepository.findByOnCallTrue();
    }

    @Override
    public StaffShift assignShift(StaffShift shift) {
        return staffShiftRepository.save(shift);
    }

    @Override
    public StaffShift toggleOnCall(Long id, boolean onCall) {
        StaffShift shift = staffShiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff shift not found with id " + id));
        shift.setOnCall(onCall);
        return staffShiftRepository.save(shift);
    }

    @Override
    public void deleteShift(Long id) {
        if (!staffShiftRepository.existsById(id)) {
            throw new ResourceNotFoundException("Staff shift not found with id " + id);
        }
        staffShiftRepository.deleteById(id);
    }
}
