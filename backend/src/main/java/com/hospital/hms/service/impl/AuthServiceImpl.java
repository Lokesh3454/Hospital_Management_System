package com.hospital.hms.service.impl;

import com.hospital.hms.dto.request.LoginRequest;
import com.hospital.hms.dto.request.RegisterRequest;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.dto.response.JwtResponse;
import com.hospital.hms.entity.*;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.RoleRepository;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.security.jwt.JwtUtils;
import com.hospital.hms.security.services.UserDetailsImpl;
import com.hospital.hms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Long profileId = null;
        if (roles.contains("ROLE_PATIENT")) {
            profileId = patientRepository.findByUserId(userDetails.getId()).map(Patient::getId).orElse(null);
        } else if (roles.contains("ROLE_DOCTOR")) {
            profileId = doctorRepository.findByUserId(userDetails.getId()).map(Doctor::getId).orElse(null);
        }

        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                roles,
                profileId
        );
    }

    @Override
    @Transactional
    public ApiResponse<String> registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Error: Username '" + registerRequest.getUsername() + "' is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Error: Email '" + registerRequest.getEmail() + "' is already in use!");
        }

        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getPhone()
        );

        String strRole = registerRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRole == null || strRole.trim().isEmpty()) {
            Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_PATIENT is not found."));
            roles.add(patientRole);
        } else {
            String roleUpper = strRole.toUpperCase().trim();
            if (!roleUpper.startsWith("ROLE_")) {
                roleUpper = "ROLE_" + roleUpper;
            }

            switch (roleUpper) {
                case "ROLE_ADMIN":
                    Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role ROLE_ADMIN is not found."));
                    roles.add(adminRole);
                    break;
                case "ROLE_DOCTOR":
                    Role doctorRole = roleRepository.findByName(RoleType.ROLE_DOCTOR)
                            .orElseThrow(() -> new RuntimeException("Error: Role ROLE_DOCTOR is not found."));
                    roles.add(doctorRole);
                    break;
                case "ROLE_RECEPTIONIST":
                    Role recRole = roleRepository.findByName(RoleType.ROLE_RECEPTIONIST)
                            .orElseThrow(() -> new RuntimeException("Error: Role ROLE_RECEPTIONIST is not found."));
                    roles.add(recRole);
                    break;
                default:
                    Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT)
                            .orElseThrow(() -> new RuntimeException("Error: Role ROLE_PATIENT is not found."));
                    roles.add(patientRole);
                    break;
            }
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // If registered as patient, automatically initialize a patient profile
        boolean isPatient = roles.stream().anyMatch(r -> r.getName() == RoleType.ROLE_PATIENT);
        if (isPatient) {
            Patient patient = new Patient();
            patient.setUser(savedUser);
            patient.setGender(registerRequest.getGender());
            patient.setBloodGroup(registerRequest.getBloodGroup());
            patient.setAddress(registerRequest.getAddress());
            patient.setEmergencyContact(registerRequest.getPhone());
            patientRepository.save(patient);
        }

        // If registered as doctor, initialize a doctor profile
        boolean isDoctor = roles.stream().anyMatch(r -> r.getName() == RoleType.ROLE_DOCTOR);
        if (isDoctor) {
            Doctor doctor = new Doctor();
            doctor.setUser(savedUser);
            doctor.setSpecialization(registerRequest.getSpecialization() != null ? registerRequest.getSpecialization() : "General Physician");
            doctor.setDepartment(registerRequest.getDepartment() != null ? registerRequest.getDepartment() : "General Medicine");
            doctor.setConsultationFee(BigDecimal.valueOf(500.00));
            doctor.setExperienceYears(3);
            doctorRepository.save(doctor);
        }

        return ApiResponse.ok("User registered successfully!");
    }
}
