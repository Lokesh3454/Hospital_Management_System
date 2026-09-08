package com.hospital.hms.service.impl;

import com.hospital.hms.dto.BedDTO;
import com.hospital.hms.entity.Bed;
import com.hospital.hms.entity.BedStatus;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.BedRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BedServiceImpl implements BedService {

    private final BedRepository bedRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public BedServiceImpl(BedRepository bedRepository, PatientRepository patientRepository) {
        this.bedRepository = bedRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedDTO> getAllBeds() {
        return bedRepository.findAll()
                .stream()
                .map(BedDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BedDTO getBedById(Long id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with id: " + id));
        return BedDTO.fromEntity(bed);
    }

    @Override
    @Transactional
    public BedDTO createBed(BedDTO dto) {
        Bed bed = new Bed();
        bed.setBedNumber(dto.getBedNumber());
        bed.setWardType(dto.getWardType());
        bed.setRoomNumber(dto.getRoomNumber());
        bed.setStatus(dto.getStatus() != null ? dto.getStatus() : BedStatus.AVAILABLE);
        bed.setDailyRate(dto.getDailyRate() != null ? dto.getDailyRate() : 1000.0);
        bed.setNotes(dto.getNotes());
        Bed saved = bedRepository.save(bed);
        return BedDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public BedDTO updateBed(Long id, BedDTO dto) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with id: " + id));

        if (dto.getBedNumber() != null) bed.setBedNumber(dto.getBedNumber());
        if (dto.getWardType() != null) bed.setWardType(dto.getWardType());
        if (dto.getRoomNumber() != null) bed.setRoomNumber(dto.getRoomNumber());
        if (dto.getStatus() != null) bed.setStatus(dto.getStatus());
        if (dto.getDailyRate() != null) bed.setDailyRate(dto.getDailyRate());
        if (dto.getNotes() != null) bed.setNotes(dto.getNotes());

        Bed saved = bedRepository.save(bed);
        return BedDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public BedDTO assignPatientToBed(Long bedId, Long patientId, String notes) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with id: " + bedId));

        if (bed.getStatus() == BedStatus.OCCUPIED) {
            throw new BadRequestException("Bed " + bed.getBedNumber() + " is already occupied.");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        bed.setCurrentPatient(patient);
        bed.setStatus(BedStatus.OCCUPIED);
        bed.setAdmissionDate(LocalDateTime.now());
        if (notes != null && !notes.trim().isEmpty()) {
            bed.setNotes(notes);
        }

        Bed saved = bedRepository.save(bed);
        return BedDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public BedDTO dischargeBed(Long bedId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with id: " + bedId));

        bed.setCurrentPatient(null);
        bed.setStatus(BedStatus.CLEANING); // Set to cleaning post-discharge
        bed.setAdmissionDate(null);
        Bed saved = bedRepository.save(bed);
        return BedDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteBed(Long id) {
        if (!bedRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bed not found with id: " + id);
        }
        bedRepository.deleteById(id);
    }
}
