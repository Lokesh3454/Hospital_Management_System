package com.hospital.hms.service.impl;

import com.hospital.hms.dto.VitalsDTO;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.entity.Vitals;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.VitalsRepository;
import com.hospital.hms.service.VitalsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VitalsServiceImpl implements VitalsService {

    private final VitalsRepository vitalsRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public VitalsServiceImpl(VitalsRepository vitalsRepository, PatientRepository patientRepository) {
        this.vitalsRepository = vitalsRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public VitalsDTO recordVitals(VitalsDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        Vitals v = new Vitals();
        v.setPatient(patient);
        v.setRecordedBy(dto.getRecordedBy() != null ? dto.getRecordedBy() : "Staff");
        v.setSystolicBP(dto.getSystolicBP());
        v.setDiastolicBP(dto.getDiastolicBP());
        v.setHeartRate(dto.getHeartRate());
        v.setTemperature(dto.getTemperature());
        v.setSpo2(dto.getSpo2());
        v.setRespiratoryRate(dto.getRespiratoryRate());
        v.setWeightKg(dto.getWeightKg());
        v.setHeightCm(dto.getHeightCm());

        // Calculate BMI if height and weight are provided
        if (dto.getWeightKg() != null && dto.getHeightCm() != null && dto.getHeightCm() > 0) {
            double heightM = dto.getHeightCm() / 100.0;
            double bmi = Math.round((dto.getWeightKg() / (heightM * heightM)) * 10.0) / 10.0;
            v.setBmi(bmi);
        } else {
            v.setBmi(dto.getBmi());
        }

        v.setNotes(dto.getNotes());
        v.setRecordedAt(dto.getRecordedAt() != null ? dto.getRecordedAt() : LocalDateTime.now());

        Vitals saved = vitalsRepository.save(v);
        return VitalsDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalsDTO> getVitalsByPatient(Long patientId) {
        return vitalsRepository.findByPatientIdOrderByRecordedAtDesc(patientId)
                .stream()
                .map(VitalsDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VitalsDTO getLatestVitalsByPatient(Long patientId) {
        return vitalsRepository.findTopByPatientIdOrderByRecordedAtDesc(patientId)
                .map(VitalsDTO::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional
    public void deleteVitals(Long id) {
        if (!vitalsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vitals record not found with id: " + id);
        }
        vitalsRepository.deleteById(id);
    }
}
