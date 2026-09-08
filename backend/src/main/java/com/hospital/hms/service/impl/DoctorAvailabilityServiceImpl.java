package com.hospital.hms.service.impl;

import com.hospital.hms.dto.DoctorAvailabilityDTO;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.entity.DoctorAvailability;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.DoctorAvailabilityRepository;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.service.DoctorAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DoctorAvailabilityDTO> getAvailabilitiesByDoctorId(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }
        return availabilityRepository.findByDoctorId(doctorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoctorAvailabilityDTO addAvailability(Long doctorId, DoctorAvailabilityDTO dto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().equals(dto.getEndTime())) {
            throw new BadRequestException("Start time must be before end time.");
        }

        String dayOfWeek = dto.getDayOfWeek().toUpperCase().trim();

        // Check if availability for that day already exists
        availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek).ifPresent(existing -> {
            throw new BadRequestException("Availability for " + dayOfWeek + " already exists. Please update the existing entry.");
        });

        DoctorAvailability availability = new DoctorAvailability(
                doctor,
                dayOfWeek,
                dto.getStartTime(),
                dto.getEndTime(),
                dto.isAvailable()
        );

        DoctorAvailability saved = availabilityRepository.save(availability);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public DoctorAvailabilityDTO updateAvailability(Long availabilityId, DoctorAvailabilityDTO dto) {
        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));

        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().equals(dto.getEndTime())) {
                throw new BadRequestException("Start time must be before end time.");
            }
            availability.setStartTime(dto.getStartTime());
            availability.setEndTime(dto.getEndTime());
        }

        if (dto.getDayOfWeek() != null && !dto.getDayOfWeek().trim().isEmpty()) {
            availability.setDayOfWeek(dto.getDayOfWeek().toUpperCase().trim());
        }

        availability.setAvailable(dto.isAvailable());

        DoctorAvailability updated = availabilityRepository.save(availability);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteAvailability(Long availabilityId) {
        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));
        availabilityRepository.delete(availability);
    }

    private DoctorAvailabilityDTO mapToDTO(DoctorAvailability a) {
        String docName = (a.getDoctor() != null && a.getDoctor().getUser() != null)
                ? (a.getDoctor().getUser().getFirstName() + " " + a.getDoctor().getUser().getLastName()).trim()
                : "Unknown";

        return new DoctorAvailabilityDTO(
                a.getId(),
                a.getDoctor() != null ? a.getDoctor().getId() : null,
                docName,
                a.getDayOfWeek(),
                a.getStartTime(),
                a.getEndTime(),
                a.isAvailable()
        );
    }
}
