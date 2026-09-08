package com.hospital.hms.service;

import com.hospital.hms.dto.DoctorAvailabilityDTO;
import java.util.List;

public interface DoctorAvailabilityService {

    List<DoctorAvailabilityDTO> getAvailabilitiesByDoctorId(Long doctorId);

    DoctorAvailabilityDTO addAvailability(Long doctorId, DoctorAvailabilityDTO dto);

    DoctorAvailabilityDTO updateAvailability(Long availabilityId, DoctorAvailabilityDTO dto);

    void deleteAvailability(Long availabilityId);
}
