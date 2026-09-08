package com.hospital.hms.service.impl;

import com.hospital.hms.dto.LabTestDTO;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.entity.LabTest;
import com.hospital.hms.entity.LabTestStatus;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.LabTestRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.service.LabTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabTestServiceImpl implements LabTestService {

    private final LabTestRepository labTestRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Autowired
    public LabTestServiceImpl(LabTestRepository labTestRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.labTestRepository = labTestRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestDTO> getAllLabTests() {
        return labTestRepository.findAll()
                .stream()
                .map(LabTestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestDTO> getLabTestsByPatient(Long patientId) {
        return labTestRepository.findByPatientIdOrderByOrderDateDesc(patientId)
                .stream()
                .map(LabTestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestDTO> getLabTestsByDoctor(Long doctorId) {
        return labTestRepository.findByDoctorIdOrderByOrderDateDesc(doctorId)
                .stream()
                .map(LabTestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LabTestDTO getLabTestById(Long id) {
        LabTest lt = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id));
        return LabTestDTO.fromEntity(lt);
    }

    @Override
    @Transactional
    public LabTestDTO createLabTest(LabTestDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        Doctor doctor = null;
        if (dto.getDoctorId() != null) {
            doctor = doctorRepository.findById(dto.getDoctorId()).orElse(null);
        }

        LabTest lt = new LabTest();
        lt.setTestCode(dto.getTestCode() != null ? dto.getTestCode() : "LAB-" + System.currentTimeMillis() % 10000);
        lt.setTestName(dto.getTestName());
        lt.setCategory(dto.getCategory() != null ? dto.getCategory() : "General Diagnostics");
        lt.setPatient(patient);
        lt.setDoctor(doctor);
        lt.setOrderDate(LocalDateTime.now());
        lt.setStatus(LabTestStatus.ORDERED);
        lt.setNormalRange(dto.getNormalRange());
        lt.setUnit(dto.getUnit());
        lt.setCost(dto.getCost() != null ? dto.getCost() : 0.0);
        lt.setRemarks(dto.getRemarks());

        LabTest saved = labTestRepository.save(lt);
        return LabTestDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public LabTestDTO updateLabTestStatus(Long id, String statusStr) {
        LabTest lt = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id));

        try {
            LabTestStatus newStatus = LabTestStatus.valueOf(statusStr.toUpperCase());
            lt.setStatus(newStatus);
            if (newStatus == LabTestStatus.SAMPLE_COLLECTED && lt.getCollectionDate() == null) {
                lt.setCollectionDate(LocalDateTime.now());
            } else if (newStatus == LabTestStatus.COMPLETED && lt.getCompletionDate() == null) {
                lt.setCompletionDate(LocalDateTime.now());
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + statusStr);
        }

        LabTest saved = labTestRepository.save(lt);
        return LabTestDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public LabTestDTO recordResults(Long id, String resultValue, String interpretation, String remarks) {
        LabTest lt = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id));

        lt.setResultValue(resultValue);
        lt.setInterpretation(interpretation != null ? interpretation : "Normal");
        if (remarks != null) lt.setRemarks(remarks);
        lt.setStatus(LabTestStatus.COMPLETED);
        lt.setCompletionDate(LocalDateTime.now());

        LabTest saved = labTestRepository.save(lt);
        return LabTestDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteLabTest(Long id) {
        if (!labTestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lab test not found with id: " + id);
        }
        labTestRepository.deleteById(id);
    }
}
