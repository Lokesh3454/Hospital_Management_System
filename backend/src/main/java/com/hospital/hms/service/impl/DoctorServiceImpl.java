package com.hospital.hms.service.impl;

import com.hospital.hms.dto.DoctorDTO;
import com.hospital.hms.entity.*;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.DoctorAvailabilityRepository;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.RoleRepository;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllDoctors(String search, String specialization, DoctorStatus status) {
        List<Doctor> doctors = doctorRepository.searchAndFilter(
                (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                (specialization != null && !specialization.trim().isEmpty()) ? specialization.trim() : null,
                status
        );
        return doctors.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDTO> searchAndFilter(String search, String name, String specialization, Integer minExperience, DoctorStatus status) {
        List<Doctor> doctors = doctorRepository.searchAndFilterAdvanced(
                (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                (name != null && !name.trim().isEmpty()) ? name.trim() : null,
                (specialization != null && !specialization.trim().isEmpty()) ? specialization.trim() : null,
                minExperience,
                status
        );
        return doctors.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllSpecializations() {
        return doctorRepository.findDistinctSpecializations();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return mapToDTO(doctor);
    }

    @Override
    @Transactional
    public DoctorDTO addDoctor(DoctorDTO dto) {
        String[] nameParts = splitName(dto.getName());
        String firstName = (dto.getFirstName() != null && !dto.getFirstName().trim().isEmpty()) ? dto.getFirstName().trim() : nameParts[0];
        String lastName = (dto.getLastName() != null && !dto.getLastName().trim().isEmpty()) ? dto.getLastName().trim() : nameParts[1];

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Error: Email '" + dto.getEmail() + "' is already registered.");
        }

        String baseUsername = "dr_" + dto.getEmail().split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        Role doctorRole = roleRepository.findByName(RoleType.ROLE_DOCTOR)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_DOCTOR not configured."));

        User user = new User(
                username,
                dto.getEmail(),
                passwordEncoder.encode("doctor123"),
                firstName,
                lastName,
                dto.getPhone()
        );
        user.setRoles(new HashSet<>(Collections.singletonList(doctorRole)));
        User savedUser = userRepository.save(user);

        Doctor doctor = new Doctor(
                savedUser,
                dto.getSpecialization(),
                dto.getQualification() != null ? dto.getQualification() : "MBBS, MD",
                dto.getExperience() != null ? dto.getExperience() : 0,
                dto.getConsultationFee(),
                dto.getDepartment() != null ? dto.getDepartment() : dto.getSpecialization(),
                dto.getRoomNumber() != null ? dto.getRoomNumber() : "Room 101",
                dto.getAvailableDays() != null ? dto.getAvailableDays() : "MON,TUE,WED,THU,FRI"
        );
        if (dto.getStatus() != null) {
            doctor.setStatus(dto.getStatus());
        }

        Doctor savedDoctor = doctorRepository.save(doctor);

        // Seed default weekday availability slots
        String[] defaultDays = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
        for (String day : defaultDays) {
            DoctorAvailability availability = new DoctorAvailability(
                    savedDoctor,
                    day,
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0),
                    true
            );
            availabilityRepository.save(availability);
        }

        return mapToDTO(savedDoctor);
    }

    @Override
    @Transactional
    public DoctorDTO updateDoctor(Long id, DoctorDTO dto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        User user = doctor.getUser();

        String[] nameParts = splitName(dto.getName());
        String firstName = (dto.getFirstName() != null && !dto.getFirstName().trim().isEmpty()) ? dto.getFirstName().trim() : nameParts[0];
        String lastName = (dto.getLastName() != null && !dto.getLastName().trim().isEmpty()) ? dto.getLastName().trim() : nameParts[1];

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Error: Email '" + dto.getEmail() + "' is already in use.");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        userRepository.save(user);

        doctor.setSpecialization(dto.getSpecialization());
        if (dto.getQualification() != null) doctor.setQualification(dto.getQualification());
        if (dto.getExperience() != null) doctor.setExperienceYears(dto.getExperience());
        if (dto.getConsultationFee() != null) doctor.setConsultationFee(dto.getConsultationFee());
        if (dto.getDepartment() != null) doctor.setDepartment(dto.getDepartment());
        if (dto.getRoomNumber() != null) doctor.setRoomNumber(dto.getRoomNumber());
        if (dto.getStatus() != null) doctor.setStatus(dto.getStatus());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return mapToDTO(updatedDoctor);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        User user = doctor.getUser();
        doctorRepository.delete(doctor);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    private DoctorDTO mapToDTO(Doctor d) {
        User u = d.getUser();
        String fullName = (u != null) ? (u.getFirstName() + " " + u.getLastName()).trim() : "Unknown";
        String email = (u != null) ? u.getEmail() : "";
        String phone = (u != null) ? u.getPhone() : "";

        DoctorDTO dto = new DoctorDTO(
                d.getId(),
                fullName,
                email,
                phone,
                d.getSpecialization(),
                d.getQualification(),
                d.getExperienceYears(),
                d.getConsultationFee(),
                d.getStatus()
        );
        if (u != null) {
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
        }
        dto.setDepartment(d.getDepartment());
        dto.setRoomNumber(d.getRoomNumber());
        dto.setAvailableDays(d.getAvailableDays());
        dto.setCreatedAt(d.getCreatedAt());
        return dto;
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"Doctor", ""};
        }
        String clean = fullName.replaceAll("^(Dr\\.|Dr)\\s*", "").trim();
        String[] parts = clean.split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return parts;
    }
}
