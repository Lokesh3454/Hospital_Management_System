package com.hospital.hms.service.impl;

import com.hospital.hms.dto.MedicalRecordRequestDTO;
import com.hospital.hms.dto.MedicalRecordResponseDTO;
import com.hospital.hms.entity.*;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.*;
import com.hospital.hms.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicalRecordServiceImpl implements MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Override
    public MedicalRecordResponseDTO createRecord(MedicalRecordRequestDTO request, Long authenticatedUserId) {
        if (request.getPatientId() == null) {
            throw new BadRequestException("Patient ID is required to create a medical record.");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + request.getPatientId()));

        Doctor doctor = null;
        if (request.getDoctorId() != null) {
            doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + request.getDoctorId()));
        } else if (authenticatedUserId != null) {
            doctor = doctorRepository.findByUserId(authenticatedUserId)
                    .orElseThrow(() -> new BadRequestException("Logged-in user does not have a linked doctor profile."));
        } else {
            throw new BadRequestException("Doctor ID is required.");
        }

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + request.getAppointmentId()));
        }

        MedicalRecord record = new MedicalRecord(
                patient,
                doctor,
                appointment,
                request.getDiagnosis(),
                request.getSymptoms(),
                request.getTreatment(),
                request.getTestResults(),
                request.getNotes(),
                request.getRecordDate() != null ? request.getRecordDate() : LocalDate.now()
        );

        MedicalRecord saved = medicalRecordRepository.save(record);
        return MedicalRecordResponseDTO.fromEntity(saved);
    }

    @Override
    public MedicalRecordResponseDTO updateRecord(Long id, MedicalRecordRequestDTO request, Long authenticatedUserId) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id " + id));

        // If authenticated user is a doctor, verify ownership
        if (authenticatedUserId != null) {
            Optional<Doctor> loggedInDoctor = doctorRepository.findByUserId(authenticatedUserId);
            if (loggedInDoctor.isPresent() && !loggedInDoctor.get().getId().equals(record.getDoctor().getId())) {
                throw new BadRequestException("You can only update medical records created by yourself.");
            }
        }

        if (request.getDiagnosis() != null && !request.getDiagnosis().isBlank()) {
            record.setDiagnosis(request.getDiagnosis());
        }
        if (request.getSymptoms() != null) {
            record.setSymptoms(request.getSymptoms());
        }
        if (request.getTreatment() != null) {
            record.setTreatmentPlan(request.getTreatment());
        }
        if (request.getTestResults() != null) {
            record.setTestResults(request.getTestResults());
        }
        if (request.getNotes() != null) {
            record.setNotes(request.getNotes());
        }
        if (request.getRecordDate() != null) {
            record.setRecordDate(request.getRecordDate());
        }

        MedicalRecord updated = medicalRecordRepository.save(record);
        return MedicalRecordResponseDTO.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponseDTO getRecordById(Long id, Long authenticatedUserId) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id " + id));

        // Patient privacy check
        if (authenticatedUserId != null) {
            Optional<Patient> loggedInPatient = patientRepository.findByUserId(authenticatedUserId);
            if (loggedInPatient.isPresent() && !loggedInPatient.get().getId().equals(record.getPatient().getId())) {
                throw new BadRequestException("Access denied: You can only view your own medical records.");
            }
        }

        return MedicalRecordResponseDTO.fromEntity(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDTO> getPatientHistory(Long patientId, Long authenticatedUserId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }

        // Patient privacy check
        if (authenticatedUserId != null) {
            Optional<Patient> loggedInPatient = patientRepository.findByUserId(authenticatedUserId);
            if (loggedInPatient.isPresent() && !loggedInPatient.get().getId().equals(patientId)) {
                throw new BadRequestException("Access denied: You can only view your own medical history.");
            }
        }

        return medicalRecordRepository.findByPatientIdOrderByRecordDateDescCreatedAtDesc(patientId).stream()
                .map(MedicalRecordResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDTO> getDoctorRecords(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id " + doctorId);
        }
        return medicalRecordRepository.findByDoctorIdOrderByRecordDateDescCreatedAtDesc(doctorId).stream()
                .map(MedicalRecordResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDTO> searchAndFilter(String search, LocalDate date, Long patientId, Long doctorId, Long authenticatedUserId) {
        // Enforce patient privacy
        if (authenticatedUserId != null) {
            Optional<Patient> loggedInPatient = patientRepository.findByUserId(authenticatedUserId);
            if (loggedInPatient.isPresent()) {
                patientId = loggedInPatient.get().getId();
            }
        }

        return medicalRecordRepository.searchAndFilter(search, date, patientId, doctorId).stream()
                .map(MedicalRecordResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRecord(Long id, Long authenticatedUserId) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id " + id));

        if (authenticatedUserId != null) {
            Optional<Doctor> loggedInDoctor = doctorRepository.findByUserId(authenticatedUserId);
            if (loggedInDoctor.isPresent() && !loggedInDoctor.get().getId().equals(record.getDoctor().getId())) {
                throw new BadRequestException("You can only delete medical records created by yourself.");
            }
        }

        medicalRecordRepository.delete(record);
    }

    @Override
    public void deleteAllRecords() {
        medicalRecordRepository.deleteAll();
    }

    @Override
    public void seedDiverseSampleRecords() {
        medicalRecordRepository.deleteAll();

        // 1. Ensure Diverse Doctors
        Doctor drSmith = getOrCreateDoctor("doctor_smith", "dr.smith@hospital.com", "David", "Smith",
                "Cardiology", "MD, FACC", 14, BigDecimal.valueOf(750.00), "Cardiology", "Room 302");

        Doctor drWatson = getOrCreateDoctor("doctor_watson", "dr.watson@hospital.com", "Emily", "Watson",
                "Neurology", "MD, DM, PhD", 12, BigDecimal.valueOf(900.00), "Neurology", "Suite 501");

        Doctor drVance = getOrCreateDoctor("doctor_vance", "dr.vance@hospital.com", "Marcus", "Vance",
                "Orthopedics", "MS (Ortho), FAAOS", 16, BigDecimal.valueOf(800.00), "Orthopedics & Spine", "Room 204");

        Doctor drPatel = getOrCreateDoctor("doctor_patel", "dr.patel@hospital.com", "Sophia", "Patel",
                "Endocrinology", "MD, FACE", 11, BigDecimal.valueOf(700.00), "Endocrinology & Metabolism", "Room 410");

        Doctor drKhan = getOrCreateDoctor("doctor_khan", "dr.khan@hospital.com", "Aisha", "Khan",
                "Pulmonology", "MD, FCCP", 13, BigDecimal.valueOf(650.00), "Pulmonology & Chest Medicine", "Room 315");

        Doctor drWilson = getOrCreateDoctor("doctor_wilson", "dr.wilson@hospital.com", "James", "Wilson",
                "Gastroenterology", "MD, FACG", 15, BigDecimal.valueOf(720.00), "Gastroenterology & Hepatology", "Room 108");

        // 2. Ensure Diverse Patients
        Patient priya = getOrCreatePatient("patient_priya", "priya.sharma@email.com", "Priya", "Sharma",
                "Female", "B+", LocalDate.of(1994, 3, 15), "54 Parkview Heights, Bangalore");

        Patient robert = getOrCreatePatient("patient_robert", "robert.fox@email.com", "Robert", "Fox",
                "Male", "B+", LocalDate.of(1988, 11, 20), "789 Pine Street, Denver CO");

        Patient alice = getOrCreatePatient("patient_alice", "alice.wonder@email.com", "Alice", "Wonder",
                "Female", "A+", LocalDate.of(1996, 8, 22), "123 Wonderland Ave, Seattle WA");

        Patient carlos = getOrCreatePatient("patient_carlos", "carlos.mendez@email.com", "Carlos", "Mendez",
                "Male", "A-", LocalDate.of(1965, 9, 18), "404 Sunset Boulevard, Miami FL");

        Patient michael = getOrCreatePatient("patient_michael", "michael.chang@email.com", "Michael", "Chang",
                "Male", "AB+", LocalDate.of(1974, 7, 9), "215 Lakeview Terrace, Chicago IL");

        Patient clara = getOrCreatePatient("patient_clara", "clara.oswald@email.com", "Clara", "Oswald",
                "Female", "O-", LocalDate.of(1997, 12, 4), "10 Baker Street, Boston MA");

        Patient sarah = getOrCreatePatient("patient_sarah_pat", "sarah.jenkins@email.com", "Sarah", "Jenkins",
                "Female", "O+", LocalDate.of(1986, 4, 30), "88 Meadowbrook Lane, Austin TX");

        Patient john = getOrCreatePatient("patient_john", "john.doe@email.com", "John", "Doe",
                "Male", "O+", LocalDate.of(1990, 5, 14), "742 Evergreen Terrace, Springfield");

        // 3. Create Diverse Medical Records
        LocalDate today = LocalDate.now();

        medicalRecordRepository.save(new MedicalRecord(
                priya, drPatel, null,
                "Type 2 Diabetes Mellitus with Mild Neuropathy",
                "Polyuria, polydipsia, persistent fatigue, bilateral foot tingling and numbness",
                "Metformin 500mg BID with meals, Gabapentin 100mg QHS, carbohydrate-controlled diabetic diet, daily 30-min brisk walk",
                "HbA1c: 8.4%, Fasting Glucose: 172 mg/dL, Postprandial Glucose: 238 mg/dL, Urine Microalbumin: 32 mg/g",
                "Nutritional counseling completed. Daily foot self-inspection demonstrated. Follow-up HbA1c review in 90 days.",
                today.minusDays(1)
        ));

        medicalRecordRepository.save(new MedicalRecord(
                robert, drVance, null,
                "Lumbar Disc Herniation (L5-S1) & Left Sciatica",
                "Severe lower back pain radiating down left posterior thigh, paresthesia in lateral foot, exacerbated by sitting",
                "Targeted lumbar core stabilization physical therapy, Meloxicam 15mg daily with food, ergonomic lumbar support",
                "Lumbar Spine MRI: 4.2mm posterolateral disc protrusion at L5-S1 compressing S1 nerve root. SLR positive on left at 40°",
                "Conservative non-surgical protocol initiated for 6 weeks. Avoid heavy lifting and prolonged trunk flexion.",
                today.minusDays(2)
        ));

        medicalRecordRepository.save(new MedicalRecord(
                alice, drWatson, null,
                "Migraine with Visual Aura & Vestibular Symptoms",
                "Unilateral throbbing hemicranial headache, scintillating zig-zag scotoma, marked photophobia, nausea, vertigo",
                "Sumatriptan 50mg PO at onset of aura, Propranolol 40mg daily prophylaxis, dim room rest, consistent sleep hygiene",
                "Brain MRI with contrast: Normal parenchymal architecture, no vascular lesions or mass effect. Cranial nerves intact.",
                "Headache diary issued. Advised patient to avoid aged cheeses, red wine, and irregular meal intervals.",
                today.minusDays(4)
        ));

        medicalRecordRepository.save(new MedicalRecord(
                carlos, drKhan, null,
                "Moderate Persistent Asthma with Acute Exacerbation",
                "Nocturnal wheezing, paroxysmal coughing episodes, chest tightness, shortness of breath on stairs",
                "Budesonide/Formoterol 160/4.5mcg 2 puffs BID, Albuterol HFA PRN for acute bronchospasm, oral Prednisone 40mg taper",
                "Spirometry: FEV1 66% predicted, FEV1/FVC 0.68, post-bronchodilator improvement of 15% (+280mL). SpO2: 96% on room air",
                "Asthma Action Plan formulated with green/yellow/red zones. Metered-dose inhaler spacer technique reviewed.",
                today.minusDays(6)
        ));

        medicalRecordRepository.save(new MedicalRecord(
                michael, drSmith, null,
                "Coronary Artery Disease & Stable Exertional Angina",
                "Retrosternal chest pressure radiating to left shoulder on brisk walking, relieved by resting within 3 minutes",
                "Aspirin 81mg daily, Atorvastatin 40mg daily, Metoprolol Succinate 50mg daily, sublingual Nitroglycerin 0.4mg PRN",
                "Stress Echocardiogram: 1.5mm ST depression in leads II, III, aVF at 8.2 METs. LVEF: 54% with inferior wall hypokinesia",
                "Elective cardiac CT angiography scheduled. Patient instructed to call emergency services if angina persists > 10 minutes.",
                today.minusDays(10)
        ));

        medicalRecordRepository.save(new MedicalRecord(
                clara, drWilson, null,
                "Acute Infectious Gastroenteritis & Mild Dehydration",
                "Watery diarrhea (7 episodes/day), low-grade pyrexia 100.8F, periumbilical cramping, nausea, dry oral mucosa",
                "Oral Rehydration Solution (ORS) 2-3 L/day, Ciprofloxacin 500mg BID for 3 days, probiotics, light bland BRAT diet",
                "Stool PCR & microscopy: Moderate fecal leukocytes, negative for C. difficile toxins. Serum Na: 138, K: 3.7 mEq/L",
                "Symptoms developed after restaurant seafood meal. Clinical recovery noted after 24 hours of hydration therapy.",
                today.minusDays(13)
        ));

        medicalRecordRepository.save(new MedicalRecord(
                sarah, drPatel, null,
                "Rheumatoid Arthritis (Seropositive, Active Stage)",
                "Bilateral symmetric wrist and MCP joint pain with morning stiffness lasting > 75 minutes, generalized fatigue",
                "Methotrexate 15mg weekly, Folic Acid 5mg weekly, Celecoxib 100mg BID with meals, occupational hand therapy",
                "Rheumatoid Factor: 86 IU/mL (positive), Anti-CCP: 142 U/mL (strongly positive), ESR: 46 mm/hr, CRP: 24.5 mg/L",
                "Baseline hepatic enzyme panel and complete blood count within normal limits. Routine lab re-evaluation in 4 weeks.",
                today.minusDays(18)
        ));

        medicalRecordRepository.save(new MedicalRecord(
                john, drSmith, null,
                "Essential Hypertension Stage 1 (Controlled on Therapy)",
                "Occasional mild occipital tension headache after working late, no palpitations, dizziness, or chest discomfort",
                "Telmisartan 40mg once daily in morning, dietary sodium restriction (<2g/day), DASH protocol, evening aerobic walk",
                "Resting Blood Pressure: 126/82 mmHg (improved from 152/94 mmHg), Serum Creatinine: 0.9 mg/dL, Serum Potassium: 4.3 mEq/L",
                "Home blood pressure log demonstrates excellent compliance and BP stability. Scheduled 6-month routine review.",
                today.minusDays(23)
        ));
    }

    private Doctor getOrCreateDoctor(String username, String email, String firstName, String lastName,
                                     String specialization, String qualification, Integer experience,
                                     BigDecimal fee, String department, String room) {
        return userRepository.findByUsernameOrEmail(username, email)
                .flatMap(u -> doctorRepository.findByUserId(u.getId()))
                .orElseGet(() -> {
                    Role doctorRole = roleRepository.findByName(RoleType.ROLE_DOCTOR)
                            .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_DOCTOR)));

                    User user = userRepository.findByUsernameOrEmail(username, email).orElseGet(() -> {
                        User u = new User(username, email, passwordEncoder.encode("doctor123"), firstName, lastName, "+1-555-" + (1000 + (int)(Math.random() * 8999)));
                        u.setRoles(new HashSet<>(Collections.singletonList(doctorRole)));
                        return userRepository.save(u);
                    });

                    return doctorRepository.findByUserId(user.getId()).orElseGet(() -> {
                        Doctor doc = new Doctor(user, specialization, qualification, experience, fee, department, room, "MON,TUE,WED,THU,FRI,SAT");
                        Doctor saved = doctorRepository.save(doc);
                        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
                        for (String day : days) {
                            LocalTime start = LocalTime.of(9, 0);
                            LocalTime end = day.equals("SATURDAY") ? LocalTime.of(14, 0) : LocalTime.of(17, 0);
                            doctorAvailabilityRepository.save(new DoctorAvailability(saved, day, start, end, true));
                        }
                        return saved;
                    });
                });
    }

    private Patient getOrCreatePatient(String username, String email, String firstName, String lastName,
                                       String gender, String bloodGroup, LocalDate dob, String address) {
        return userRepository.findByUsernameOrEmail(username, email)
                .flatMap(u -> patientRepository.findByUserId(u.getId()))
                .orElseGet(() -> {
                    Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT)
                            .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_PATIENT)));

                    User user = userRepository.findByUsernameOrEmail(username, email).orElseGet(() -> {
                        User u = new User(username, email, passwordEncoder.encode("patient123"), firstName, lastName, "+1-555-" + (2000 + (int)(Math.random() * 7999)));
                        u.setRoles(new HashSet<>(Collections.singletonList(patientRole)));
                        return userRepository.save(u);
                    });

                    return patientRepository.findByUserId(user.getId()).orElseGet(() -> {
                        Patient pat = new Patient(user, dob, gender, bloodGroup, address, "+1-555-" + (3000 + (int)(Math.random() * 6999)));
                        return patientRepository.save(pat);
                    });
                });
    }
}
