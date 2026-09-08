package com.hospital.hms.service.impl;

import com.hospital.hms.dto.PrescriptionItemDTO;
import com.hospital.hms.dto.PrescriptionRequestDTO;
import com.hospital.hms.dto.PrescriptionResponseDTO;
import com.hospital.hms.entity.*;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.*;
import com.hospital.hms.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO request, Long authenticatedUserId) {
        if (request.getPatientId() == null) {
            throw new BadRequestException("Patient ID is required to issue a prescription.");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + request.getPatientId()));

        Doctor doctor = null;
        if (request.getDoctorId() != null) {
            doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + request.getDoctorId()));
        } else if (authenticatedUserId != null) {
            doctor = doctorRepository.findByUserId(authenticatedUserId)
                    .orElseThrow(() -> new BadRequestException("Logged-in user does not have an active doctor profile."));
        } else {
            throw new BadRequestException("Doctor ID is required.");
        }

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + request.getAppointmentId()));
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("A prescription must contain at least one medication item.");
        }

        Prescription prescription = new Prescription(
                patient,
                doctor,
                appointment,
                request.getPrescriptionDate() != null ? request.getPrescriptionDate() : LocalDate.now(),
                request.getNotes()
        );

        StringBuilder medNames = new StringBuilder();
        for (PrescriptionItemDTO itemDTO : request.getItems()) {
            PrescriptionItem item = new PrescriptionItem(
                    prescription,
                    itemDTO.getMedicineName(),
                    itemDTO.getDosage(),
                    itemDTO.getFrequency(),
                    itemDTO.getDuration(),
                    itemDTO.getInstructions()
            );
            prescription.addItem(item);
            if (medNames.length() > 0) medNames.append(", ");
            medNames.append(itemDTO.getMedicineName()).append(" (").append(itemDTO.getDosage()).append(")");
        }
        prescription.setMedications(medNames.toString());

        Prescription saved = prescriptionRepository.save(prescription);
        return PrescriptionResponseDTO.fromEntity(saved);
    }

    @Override
    public PrescriptionResponseDTO updatePrescription(Long id, PrescriptionRequestDTO request, Long authenticatedUserId) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id " + id));

        // Ownership verification for doctors
        if (authenticatedUserId != null) {
            Optional<Doctor> loggedInDoctor = doctorRepository.findByUserId(authenticatedUserId);
            if (loggedInDoctor.isPresent() && !loggedInDoctor.get().getId().equals(prescription.getDoctor().getId())) {
                throw new BadRequestException("You can only edit prescriptions issued by yourself.");
            }
        }

        if (request.getPrescriptionDate() != null) {
            prescription.setPrescriptionDate(request.getPrescriptionDate());
        }
        if (request.getNotes() != null) {
            prescription.setNotes(request.getNotes());
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            prescription.getItems().clear();
            StringBuilder medNames = new StringBuilder();
            for (PrescriptionItemDTO itemDTO : request.getItems()) {
                PrescriptionItem item = new PrescriptionItem(
                        prescription,
                        itemDTO.getMedicineName(),
                        itemDTO.getDosage(),
                        itemDTO.getFrequency(),
                        itemDTO.getDuration(),
                        itemDTO.getInstructions()
                );
                prescription.addItem(item);
                if (medNames.length() > 0) medNames.append(", ");
                medNames.append(itemDTO.getMedicineName()).append(" (").append(itemDTO.getDosage()).append(")");
            }
            prescription.setMedications(medNames.toString());
        }

        Prescription updated = prescriptionRepository.save(prescription);
        return PrescriptionResponseDTO.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDTO getPrescriptionById(Long id, Long authenticatedUserId) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id " + id));

        // Patient privacy check
        if (authenticatedUserId != null) {
            Optional<Patient> loggedInPatient = patientRepository.findByUserId(authenticatedUserId);
            if (loggedInPatient.isPresent() && !loggedInPatient.get().getId().equals(prescription.getPatient().getId())) {
                throw new BadRequestException("Access denied: You can only view your own prescriptions.");
            }
        }

        return PrescriptionResponseDTO.fromEntity(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getPatientPrescriptions(Long patientId, Long authenticatedUserId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }

        // Patient privacy check
        if (authenticatedUserId != null) {
            Optional<Patient> loggedInPatient = patientRepository.findByUserId(authenticatedUserId);
            if (loggedInPatient.isPresent() && !loggedInPatient.get().getId().equals(patientId)) {
                throw new BadRequestException("Access denied: You can only view your own prescriptions.");
            }
        }

        return prescriptionRepository.findByPatientIdOrderByPrescriptionDateDescCreatedAtDesc(patientId).stream()
                .map(PrescriptionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getDoctorPrescriptions(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id " + doctorId);
        }
        return prescriptionRepository.findByDoctorIdOrderByPrescriptionDateDescCreatedAtDesc(doctorId).stream()
                .map(PrescriptionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getAppointmentPrescriptions(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId).stream()
                .map(PrescriptionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> searchAndFilter(String search, LocalDate date, Long patientId, Long doctorId, Long authenticatedUserId) {
        // Enforce patient privacy
        if (authenticatedUserId != null) {
            Optional<Patient> loggedInPatient = patientRepository.findByUserId(authenticatedUserId);
            if (loggedInPatient.isPresent()) {
                patientId = loggedInPatient.get().getId();
            }
        }

        return prescriptionRepository.searchAndFilter(search, date, patientId, doctorId).stream()
                .map(PrescriptionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void seedDiverseSamplePrescriptions() {
        prescriptionRepository.deleteAll();

        // 1. Resolve Doctors
        List<Doctor> allDocs = doctorRepository.findAll();
        if (allDocs.isEmpty()) return;

        Doctor drSmith = findDoctorBySpecializationOrFallback("Cardiology", allDocs);
        Doctor drWatson = findDoctorBySpecializationOrFallback("Neurology", allDocs);
        Doctor drVance = findDoctorBySpecializationOrFallback("Orthopedics", allDocs);
        Doctor drPatel = findDoctorBySpecializationOrFallback("Endocrinology", allDocs);
        Doctor drKhan = findDoctorBySpecializationOrFallback("Pulmonology", allDocs);
        Doctor drWilson = findDoctorBySpecializationOrFallback("Gastroenterology", allDocs);

        // 2. Resolve Patients
        List<Patient> allPats = patientRepository.findAll();
        if (allPats.isEmpty()) return;

        Patient priya = findPatientByUsernameOrFallback("patient_priya", allPats, 0);
        Patient carlos = findPatientByUsernameOrFallback("patient_carlos", allPats, 1);
        Patient michael = findPatientByUsernameOrFallback("patient_michael", allPats, 2);
        Patient alice = findPatientByUsernameOrFallback("patient_alice", allPats, 3);
        Patient robert = findPatientByUsernameOrFallback("patient_robert", allPats, 4);
        Patient clara = findPatientByUsernameOrFallback("patient_clara", allPats, 5);
        Patient sarah = findPatientByUsernameOrFallback("patient_sarah_pat", allPats, 6);
        Patient john = findPatientByUsernameOrFallback("patient_john", allPats, 7);

        LocalDate today = LocalDate.now();

        // Rx 1: Priya Sharma with Dr. Sophia Patel (Endocrinology)
        createPrescriptionItemized(
                priya, drPatel, today.minusDays(1),
                "Type 2 Diabetes Mellitus & Metabolic Glycemic Control. Regular fasting blood glucose monitoring required.",
                List.of(
                        new String[]{"Metformin Hydrochloride", "500mg", "Twice daily with meals (BID)", "90 days", "Take with breakfast and dinner to minimize GI irritation."},
                        new String[]{"Glimepiride", "1mg", "Once daily before breakfast", "90 days", "Take 15 minutes prior to morning meal. Watch for hypoglycemia."},
                        new String[]{"Vitamin B12 (Methylcobalamin)", "1000mcg", "Once daily in morning", "90 days", "Supports diabetic peripheral nerve health."}
                )
        );

        // Rx 2: Carlos Mendez with Dr. Aisha Khan (Pulmonology)
        createPrescriptionItemized(
                carlos, drKhan, today.minusDays(2),
                "Moderate Persistent Bronchial Asthma with nocturnal wheezing. MDI spacer technique reviewed.",
                List.of(
                        new String[]{"Budesonide/Formoterol (Symbicort)", "160/4.5mcg", "2 puffs twice daily (BID)", "60 days", "Rinse mouth thoroughly with water and spit out after inhalation."},
                        new String[]{"Albuterol Sulfate Inhaler", "90mcg", "2 puffs every 4-6 hours PRN", "30 days", "Rescue inhaler for acute shortness of breath or sudden bronchospasm."},
                        new String[]{"Montelukast Sodium", "10mg", "1 tablet once daily at bedtime", "60 days", "Prevents nocturnal bronchoconstriction."}
                )
        );

        // Rx 3: Michael Chang with Dr. David Smith (Cardiology)
        createPrescriptionItemized(
                michael, drSmith, today.minusDays(4),
                "Coronary Artery Disease & Exertional Angina Prophylaxis. Low-sodium cardiac diet.",
                List.of(
                        new String[]{"Atorvastatin Calcium", "40mg", "1 tablet once daily at bedtime", "90 days", "Plaque stabilization and LDL cholesterol reduction."},
                        new String[]{"Metoprolol Succinate ER", "50mg", "1 tablet once daily in morning", "90 days", "Rate and blood pressure control. Do not abruptly discontinue."},
                        new String[]{"Aspirin Enteric Coated", "81mg", "1 tablet once daily with food", "90 days", "Antiplatelet cardioprotective maintenance therapy."},
                        new String[]{"Nitroglycerin Sublingual", "0.4mg", "1 tablet sublingually PRN for acute chest pain", "30 days", "Sit down before taking. If pain continues after 5 mins, seek emergency care."}
                )
        );

        // Rx 4: Alice Wonder with Dr. Emily Watson (Neurology)
        createPrescriptionItemized(
                alice, drWatson, today.minusDays(6),
                "Migraine with Visual Aura & Tension-type Headaches. Headache diary maintenance advised.",
                List.of(
                        new String[]{"Sumatriptan Succinate", "50mg", "1 tablet PO at first onset of aura", "15 days", "May repeat in 2 hours if headache recurs (max 200mg/24 hours)."},
                        new String[]{"Propranolol Hydrochloride", "40mg", "1 tablet once daily in morning", "60 days", "Migraine prophylactic beta-blocker."},
                        new String[]{"Magnesium Glycinate", "400mg", "1 tablet once daily at bedtime", "60 days", "Reduces neurovascular excitability and improves sleep quality."}
                )
        );

        // Rx 5: Robert Fox with Dr. Marcus Vance (Orthopedics)
        createPrescriptionItemized(
                robert, drVance, today.minusDays(8),
                "L5-S1 Lumbar Disc Herniation with Left Lumbar Radiculopathy (Sciatica). Physical therapy active.",
                List.of(
                        new String[]{"Meloxicam", "15mg", "1 tablet once daily with food", "14 days", "NSAID anti-inflammatory for sciatic nerve irritation."},
                        new String[]{"Cyclobenzaprine", "10mg", "1 tablet at bedtime PRN", "10 days", "Skeletal muscle relaxant. Causes drowsiness; avoid driving."},
                        new String[]{"Pantoprazole Sodium", "40mg", "1 tablet once daily before breakfast", "14 days", "Gastric mucosal protection during NSAID therapy."}
                )
        );

        // Rx 6: Clara Oswald with Dr. James Wilson (Gastroenterology)
        createPrescriptionItemized(
                clara, drWilson, today.minusDays(11),
                "Acute Infectious Gastroenteritis with mild dehydration. Light BRAT diet recommended.",
                List.of(
                        new String[]{"Ciprofloxacin", "500mg", "1 tablet twice daily (BID)", "5 days", "Take with plenty of fluids. Complete entire 5-day course."},
                        new String[]{"Ondansetron ODT", "4mg", "1 tablet orally disintegrating every 8 hrs PRN", "5 days", "Dissolve on tongue for antiemetic nausea control."},
                        new String[]{"Oral Rehydration Salts (ORS)", "1 sachet in 1L water", "Sip throughout the day", "5 days", "Restores fluid balance and vital electrolytes."}
                )
        );

        // Rx 7: Sarah Jenkins with Dr. Sophia Patel (Rheumatology)
        createPrescriptionItemized(
                sarah, drPatel, today.minusDays(15),
                "Seropositive Rheumatoid Arthritis in active management. Routine laboratory monitoring.",
                List.of(
                        new String[]{"Methotrexate", "15mg", "Once weekly on Sundays only", "90 days", "Take with evening meal. Never take daily. Strict weekly regimen."},
                        new String[]{"Folic Acid", "5mg", "Once weekly on Mondays (24h after MTX)", "90 days", "Counters methotrexate toxicity and reduces stomatitis."},
                        new String[]{"Celecoxib", "100mg", "1 capsule twice daily with meals", "60 days", "Selective COX-2 inhibitor for joint swelling and morning stiffness."}
                )
        );

        // Rx 8: John Doe with Dr. David Smith (Cardiology)
        createPrescriptionItemized(
                john, drSmith, today.minusDays(18),
                "Essential Hypertension Stage 1 and primary cardiovascular risk management. Target BP < 130/80.",
                List.of(
                        new String[]{"Telmisartan", "40mg", "1 tablet once daily in morning", "90 days", "Angiotensin receptor blocker. Maintain daily BP log."},
                        new String[]{"Amlodipine Besylate", "5mg", "1 tablet once daily in morning", "90 days", "Calcium channel blocker for vascular tone regulation."},
                        new String[]{"Rosuvastatin Calcium", "10mg", "1 tablet once daily at bedtime", "90 days", "Target LDL < 100 mg/dL. Report unexplained muscle soreness."}
                )
        );
    }

    private Doctor findDoctorBySpecializationOrFallback(String spec, List<Doctor> doctors) {
        return doctors.stream()
                .filter(d -> d.getSpecialization() != null && d.getSpecialization().equalsIgnoreCase(spec))
                .findFirst()
                .orElse(doctors.get(0));
    }

    private Patient findPatientByUsernameOrFallback(String username, List<Patient> patients, int fallbackIdx) {
        return patients.stream()
                .filter(p -> p.getUser() != null && username.equalsIgnoreCase(p.getUser().getUsername()))
                .findFirst()
                .orElseGet(() -> patients.get(fallbackIdx % patients.size()));
    }

    private void createPrescriptionItemized(Patient patient, Doctor doctor, LocalDate date, String notes, List<String[]> items) {
        Prescription p = new Prescription(patient, doctor, null, date, notes);
        StringBuilder meds = new StringBuilder();
        for (String[] it : items) {
            PrescriptionItem item = new PrescriptionItem(p, it[0], it[1], it[2], it[3], it[4]);
            p.addItem(item);
            if (meds.length() > 0) meds.append(", ");
            meds.append(it[0]).append(" (").append(it[1]).append(")");
        }
        p.setMedications(meds.toString());
        prescriptionRepository.save(p);
    }
}
