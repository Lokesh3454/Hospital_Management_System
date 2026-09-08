package com.hospital.hms.service.impl;

import com.hospital.hms.dto.PatientDTO;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.entity.Role;
import com.hospital.hms.entity.RoleType;
import com.hospital.hms.entity.User;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.RoleRepository;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.repository.BillRepository;
import com.hospital.hms.repository.PrescriptionRepository;
import com.hospital.hms.repository.MedicalRecordRepository;
import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.VitalsRepository;
import com.hospital.hms.repository.LabTestRepository;
import com.hospital.hms.repository.BedRepository;
import com.hospital.hms.entity.Bill;
import com.hospital.hms.entity.Prescription;
import com.hospital.hms.entity.MedicalRecord;
import com.hospital.hms.entity.Appointment;
import com.hospital.hms.entity.Vitals;
import com.hospital.hms.entity.LabTest;
import com.hospital.hms.entity.BedStatus;
import com.hospital.hms.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BillRepository billRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final VitalsRepository vitalsRepository;
    private final LabTestRepository labTestRepository;
    private final BedRepository bedRepository;

    @Autowired
    public PatientServiceImpl(PatientRepository patientRepository,
                              UserRepository userRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder,
                              BillRepository billRepository,
                              PrescriptionRepository prescriptionRepository,
                              MedicalRecordRepository medicalRecordRepository,
                              AppointmentRepository appointmentRepository,
                              VitalsRepository vitalsRepository,
                              LabTestRepository labTestRepository,
                              BedRepository bedRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.billRepository = billRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.vitalsRepository = vitalsRepository;
        this.labTestRepository = labTestRepository;
        this.bedRepository = bedRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients(String search, String gender, String bloodGroup) {
        List<Patient> patients = patientRepository.searchAndFilter(
                (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                (gender != null && !gender.trim().isEmpty()) ? gender.trim() : null,
                (bloodGroup != null && !bloodGroup.trim().isEmpty()) ? bloodGroup.trim() : null
        );

        return patients.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDTO> searchAndFilter(String search, String name, String phone, String email, String gender, String bloodGroup) {
        return searchAndFilter(search, name, phone, email, gender, bloodGroup, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDTO> searchAndFilter(String search, String name, String phone, String email, String gender, String bloodGroup, Long doctorId) {
        List<Patient> patients = patientRepository.searchAndFilterAdvanced(
                (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                (name != null && !name.trim().isEmpty()) ? name.trim() : null,
                (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null,
                (email != null && !email.trim().isEmpty()) ? email.trim() : null,
                (gender != null && !gender.trim().isEmpty()) ? gender.trim() : null,
                (bloodGroup != null && !bloodGroup.trim().isEmpty()) ? bloodGroup.trim() : null,
                doctorId
        );

        return patients.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return mapToDTO(patient);
    }

    @Override
    @Transactional
    public PatientDTO addPatient(PatientDTO dto) {
        String[] nameParts = splitName(dto.getName());
        String firstName = (dto.getFirstName() != null && !dto.getFirstName().trim().isEmpty()) ? dto.getFirstName().trim() : nameParts[0];
        String lastName = (dto.getLastName() != null && !dto.getLastName().trim().isEmpty()) ? dto.getLastName().trim() : nameParts[1];

        // Check if email already registered in users
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Error: Email '" + dto.getEmail() + "' is already registered to an account.");
        }

        // Generate username from email or name
        String baseUsername = dto.getEmail().split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_PATIENT not configured."));

        User user = new User(
                username,
                dto.getEmail(),
                passwordEncoder.encode("patient123"),
                firstName,
                lastName,
                dto.getPhone()
        );
        user.setRoles(new HashSet<>(Collections.singletonList(patientRole)));
        User savedUser = userRepository.save(user);

        Patient patient = new Patient(
                savedUser,
                dto.getDateOfBirth(),
                dto.getGender(),
                dto.getBloodGroup(),
                dto.getAddress(),
                dto.getEmergencyContact()
        );
        patient.setAllergies(dto.getAllergies());
        patient.setChronicConditions(dto.getChronicConditions());
        Patient savedPatient = patientRepository.save(patient);

        return mapToDTO(savedPatient);
    }

    @Override
    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        User user = patient.getUser();

        String[] nameParts = splitName(dto.getName());
        String firstName = (dto.getFirstName() != null && !dto.getFirstName().trim().isEmpty()) ? dto.getFirstName().trim() : nameParts[0];
        String lastName = (dto.getLastName() != null && !dto.getLastName().trim().isEmpty()) ? dto.getLastName().trim() : nameParts[1];

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Error: Email '" + dto.getEmail() + "' is already in use by another user.");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        userRepository.save(user);

        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setAllergies(dto.getAllergies());
        patient.setChronicConditions(dto.getChronicConditions());

        Patient updatedPatient = patientRepository.save(patient);
        return mapToDTO(updatedPatient);
    }

    @Override
    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        // 1. Delete associated bills
        List<Bill> bills = billRepository.findByPatientIdOrderByBillingDateDesc(id);
        if (!bills.isEmpty()) {
            billRepository.deleteAll(bills);
        }

        // 2. Delete associated prescriptions (cascades to prescription items)
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByPrescriptionDateDescCreatedAtDesc(id);
        if (!prescriptions.isEmpty()) {
            prescriptionRepository.deleteAll(prescriptions);
        }

        // 3. Delete associated medical records
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDescCreatedAtDesc(id);
        if (!records.isEmpty()) {
            medicalRecordRepository.deleteAll(records);
        }

        // 4. Delete associated appointments
        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(id);
        if (!appointments.isEmpty()) {
            appointmentRepository.deleteAll(appointments);
        }

        // 5. Delete associated vitals
        List<Vitals> vitals = vitalsRepository.findByPatientId(id);
        if (!vitals.isEmpty()) {
            vitalsRepository.deleteAll(vitals);
        }

        // 6. Delete associated lab tests
        List<LabTest> labTests = labTestRepository.findByPatientId(id);
        if (!labTests.isEmpty()) {
            labTestRepository.deleteAll(labTests);
        }

        // 7. Unassign any occupied bed
        bedRepository.findByCurrentPatientId(id).ifPresent(b -> {
            b.setCurrentPatient(null);
            b.setStatus(BedStatus.AVAILABLE);
            b.setAdmissionDate(null);
            bedRepository.save(b);
        });

        // 8. Delete patient entity
        User user = patient.getUser();
        patientRepository.delete(patient);

        // 9. Delete user account
        if (user != null) {
            userRepository.delete(user);
        }
    }

    private PatientDTO mapToDTO(Patient p) {
        User u = p.getUser();
        String fullName = (u != null) ? (u.getFirstName() + " " + u.getLastName()).trim() : "Unknown";
        String email = (u != null) ? u.getEmail() : "";
        String phone = (u != null) ? u.getPhone() : "";

        PatientDTO dto = new PatientDTO(
                p.getId(),
                fullName,
                p.getDateOfBirth(),
                p.getGender(),
                phone,
                email,
                p.getAddress(),
                p.getBloodGroup(),
                p.getEmergencyContact(),
                p.getCreatedAt()
        );
        dto.setAllergies(p.getAllergies());
        dto.setChronicConditions(p.getChronicConditions());
        if (u != null) {
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
        }
        return dto;
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"Patient", ""};
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return parts;
    }
}
