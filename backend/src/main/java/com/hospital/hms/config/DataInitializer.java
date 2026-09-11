package com.hospital.hms.config;

import com.hospital.hms.entity.*;
import com.hospital.hms.repository.*;
import com.hospital.hms.service.MedicalRecordService;
import com.hospital.hms.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private com.hospital.hms.service.AppointmentService appointmentService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private VitalsRepository vitalsRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private EmergencyCaseRepository emergencyCaseRepository;

    @Autowired
    private SurgeryScheduleRepository surgeryScheduleRepository;

    @Autowired
    private BloodInventoryRepository bloodInventoryRepository;

    @Autowired
    private InsuranceClaimRepository insuranceClaimRepository;

    @Autowired
    private StaffShiftRepository staffShiftRepository;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Initializing system roles and default demo users...");

        // 1. Initialize Roles
        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_ADMIN)));

        Role doctorRole = roleRepository.findByName(RoleType.ROLE_DOCTOR)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_DOCTOR)));

        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_PATIENT)));

        Role receptionistRole = roleRepository.findByName(RoleType.ROLE_RECEPTIONIST)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_RECEPTIONIST)));

        // 2. Initialize Admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@hospital.com", passwordEncoder.encode("admin123"), "System", "Administrator", "+1-555-0100");
            admin.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
            userRepository.save(admin);
            logger.info("Created default Admin user: admin / admin123");
        }

        // 3. Initialize Doctor
        Doctor savedDoctor = null;
        if (!userRepository.existsByUsername("doctor_smith")) {
            User docUser = new User("doctor_smith", "dr.smith@hospital.com", passwordEncoder.encode("doctor123"), "David", "Smith", "+1-555-0101");
            docUser.setRoles(new HashSet<>(Collections.singletonList(doctorRole)));
            User savedDocUser = userRepository.save(docUser);

            Doctor doctor = new Doctor(savedDocUser, "Cardiology", "MD, FACC", 12, BigDecimal.valueOf(750.00), "Cardiology", "Room 302", "MON,TUE,WED,THU,FRI");
            savedDoctor = doctorRepository.save(doctor);
            logger.info("Created default Doctor user: doctor_smith / doctor123");
        } else {
            savedDoctor = doctorRepository.findByUserId(userRepository.findByUsername("doctor_smith").get().getId()).orElse(null);
        }

        // 3.1 Initialize Doctor Availability for all doctors if not present
        String[] defaultDays = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        for (Doctor doc : doctorRepository.findAll()) {
            if (doctorAvailabilityRepository.findByDoctorId(doc.getId()).isEmpty()) {
                for (String day : defaultDays) {
                    LocalTime start = LocalTime.of(9, 0);
                    LocalTime end = day.equals("SATURDAY") ? LocalTime.of(14, 0) : LocalTime.of(17, 0);
                    DoctorAvailability da = new DoctorAvailability(doc, day, start, end, true);
                    doctorAvailabilityRepository.save(da);
                }
                logger.info("Seeded default working hours (09:00 - 17:00 MON-FRI, 09:00 - 14:00 SAT) for Dr. {}",
                        doc.getUser() != null ? doc.getUser().getLastName() : doc.getId());
            }
        }

        // 4. Initialize Patient
        Patient savedPatient = null;
        if (!userRepository.existsByUsername("patient_john")) {
            User patUser = new User("patient_john", "john.doe@email.com", passwordEncoder.encode("patient123"), "John", "Doe", "+1-555-0102");
            patUser.setRoles(new HashSet<>(Collections.singletonList(patientRole)));
            User savedPatUser = userRepository.save(patUser);

            Patient patient = new Patient(savedPatUser, LocalDate.of(1990, 5, 14), "Male", "O+", "742 Evergreen Terrace, Springfield", "+1-555-0199");
            savedPatient = patientRepository.save(patient);
            logger.info("Created default Patient user: patient_john / patient123");
        } else {
            savedPatient = patientRepository.findByUserId(userRepository.findByUsername("patient_john").get().getId()).orElse(null);
        }

        // 5. Initialize Receptionist
        if (!userRepository.existsByUsername("receptionist_sarah")) {
            User recUser = new User("receptionist_sarah", "sarah.rec@hospital.com", passwordEncoder.encode("rec123"), "Sarah", "Jenkins", "+1-555-0103");
            recUser.setRoles(new HashSet<>(Collections.singletonList(receptionistRole)));
            userRepository.save(recUser);
            logger.info("Created default Receptionist user: receptionist_sarah / rec123");
        }

        // 6. Initialize Sample Appointment, Medical Record, Prescription, and Bill for relational integrity demo
        if (savedDoctor != null && savedPatient != null && appointmentRepository.count() == 0) {
            Appointment appointment = new Appointment(
                    savedPatient,
                    savedDoctor,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(10, 30),
                    AppointmentStatus.CONFIRMED,
                    "Routine cardiac checkup and blood pressure monitoring",
                    "Patient reported occasional dizziness. Fasting blood test required prior to visit."
            );
            Appointment savedAppointment = appointmentRepository.save(appointment);

            medicalRecordService.seedDiverseSampleRecords();
            logger.info("Initialized diverse sample medical records across multiple specialties, diagnoses, and patients.");

            Prescription prescription = new Prescription(
                    savedPatient,
                    savedDoctor,
                    savedAppointment,
                    LocalDate.now(),
                    "Follow prescribed dosage for 30 days. Avoid grapefruit juice with Amlodipine."
            );
            prescription.addItem(new PrescriptionItem(prescription, "Amlodipine", "5mg", "Once daily (Morning)", "30 days", "Take with water after breakfast."));
            prescription.addItem(new PrescriptionItem(prescription, "Multivitamin Gold", "1 capsule", "Once daily (Evening)", "30 days", "Take after dinner."));
            prescriptionRepository.save(prescription);

            Bill bill = new Bill(
                    savedPatient,
                    savedAppointment,
                    BigDecimal.valueOf(750.00),
                    BigDecimal.valueOf(120.00),
                    BigDecimal.valueOf(350.00),
                    BigDecimal.valueOf(50.00),
                    PaymentStatus.PAID,
                    "CARD"
            );
            bill.setBillNumber("BILL-2024-00001");
            bill.setNotes("Cardiac follow-up consultation and lab testing charges.");
            billRepository.save(bill);

            if (billRepository.count() <= 1) {
                Bill pendingBill = new Bill(
                        savedPatient,
                        null,
                        BigDecimal.valueOf(500.00),
                        BigDecimal.valueOf(75.00),
                        BigDecimal.valueOf(125.00),
                        BigDecimal.valueOf(0.00),
                        PaymentStatus.PENDING,
                        null
                );
                pendingBill.setBillNumber("BILL-2024-00002");
                pendingBill.setNotes("General physician routine wellness evaluation invoice.");
                billRepository.save(pendingBill);
            }

            logger.info("Created sample Appointment, Medical Record, Prescription, and Bill linking Patient and Doctor.");
        }

        if (prescriptionRepository.count() == 0) {
            prescriptionService.seedDiverseSamplePrescriptions();
            logger.info("Initialized diverse sample prescriptions across multiple specialties, doctors, and patients.");
        }

        appointmentService.seedDiverseSampleAppointments();
        logger.info("Initialized diverse active upcoming and historical appointments across doctors and patients.");

        // Seed Patient allergies if empty
        java.util.List<Patient> patients = patientRepository.findAll();
        if (!patients.isEmpty()) {
            Patient p0 = patients.get(0);
            if (p0.getAllergies() == null || p0.getAllergies().isEmpty()) {
                p0.setAllergies("Penicillin, Aspirin");
                p0.setChronicConditions("Type 2 Diabetes, Hypertension");
                patientRepository.save(p0);
            }
            if (patients.size() > 1) {
                Patient p1 = patients.get(1);
                if (p1.getAllergies() == null || p1.getAllergies().isEmpty()) {
                    p1.setAllergies("Sulfa drugs");
                    p1.setChronicConditions("Asthma");
                    patientRepository.save(p1);
                }
            }
        }

        // Seed Vitals
        if (vitalsRepository.count() == 0 && !patients.isEmpty()) {
            Patient p1 = patients.get(0);
            vitalsRepository.save(new Vitals(p1, "Dr. Sarah Jenkins", 120, 80, 72, 98.6, 99, 16, 68.5, 172.0, 23.2, "Normal resting vitals. Patient comfortable."));
            vitalsRepository.save(new Vitals(p1, "Nurse Emily", 128, 84, 76, 99.1, 98, 18, 69.0, 172.0, 23.3, "Slightly elevated BP post exertion."));
            if (patients.size() > 1) {
                Patient p2 = patients.get(1);
                vitalsRepository.save(new Vitals(p2, "Dr. James Wilson", 118, 76, 68, 98.4, 99, 14, 62.0, 165.0, 22.8, "Baseline vitals within optimal range."));
            }
            logger.info("Seeded sample patient vitals.");
        }

        // Seed Beds
        if (bedRepository.count() == 0) {
            Patient admittedPatient = !patients.isEmpty() ? patients.get(0) : null;
            Bed b1 = new Bed("ICU-101", WardType.ICU, "ICU Critical Wing A", BedStatus.OCCUPIED, 6500.0);
            b1.setCurrentPatient(admittedPatient);
            b1.setAdmissionDate(java.time.LocalDateTime.now().minusDays(2));
            b1.setNotes("Post-cardiac intervention observation. Continuous telemetry.");
            bedRepository.save(b1);

            bedRepository.save(new Bed("ICU-102", WardType.ICU, "ICU Critical Wing A", BedStatus.AVAILABLE, 6500.0));
            bedRepository.save(new Bed("EMG-01", WardType.EMERGENCY, "Trauma Center Bay 1", BedStatus.AVAILABLE, 3500.0));
            bedRepository.save(new Bed("EMG-02", WardType.EMERGENCY, "Trauma Center Bay 2", BedStatus.CLEANING, 3500.0));
            bedRepository.save(new Bed("GEN-201", WardType.GENERAL, "General Ward 2nd Fl, Bed A", BedStatus.AVAILABLE, 1200.0));
            bedRepository.save(new Bed("GEN-202", WardType.GENERAL, "General Ward 2nd Fl, Bed B", BedStatus.AVAILABLE, 1200.0));
            bedRepository.save(new Bed("SEMI-301", WardType.SEMI_PRIVATE, "Private Wing 3rd Fl, Rm 301", BedStatus.AVAILABLE, 2800.0));
            bedRepository.save(new Bed("MAT-401", WardType.MATERNITY, "Mother & Child Wing 4th Fl", BedStatus.AVAILABLE, 3200.0));
            logger.info("Seeded hospital inpatient wards and beds.");
        }

        // Seed Pharmacy Medicines
        if (medicineRepository.count() == 0) {
            medicineRepository.save(new Medicine("Paracetamol 650mg", "Acetaminophen", "Analgesic & Antipyretic", DosageForm.TABLET, "650mg", "Cipla Ltd", "BATCH-PA-882", 2.50, 450, 50, LocalDate.now().plusMonths(18)));
            medicineRepository.save(new Medicine("Amoxicillin 500mg", "Amoxicillin Trihydrate", "Antibiotic", DosageForm.CAPSULE, "500mg", "Sun Pharma", "BATCH-AM-901", 12.00, 18, 25, LocalDate.now().plusMonths(14))); // Low stock!
            medicineRepository.save(new Medicine("Metformin 500mg", "Metformin Hydrochloride", "Antidiabetic", DosageForm.TABLET, "500mg", "USV Pvt Ltd", "BATCH-MT-404", 4.80, 320, 60, LocalDate.now().plusMonths(24)));
            medicineRepository.save(new Medicine("Atorvastatin 20mg", "Atorvastatin Calcium", "Cardiovascular", DosageForm.TABLET, "20mg", "Dr. Reddy's", "BATCH-AT-112", 15.00, 210, 40, LocalDate.now().plusMonths(16)));
            medicineRepository.save(new Medicine("Cetirizine 10mg", "Cetirizine Dihydrochloride", "Antihistamine", DosageForm.TABLET, "10mg", "Alkem Labs", "BATCH-CT-663", 3.00, 12, 30, LocalDate.now().plusMonths(11))); // Low stock!
            medicineRepository.save(new Medicine("Pantoprazole 40mg", "Pantoprazole Sodium", "Gastrointestinal", DosageForm.TABLET, "40mg", "Lupin Ltd", "BATCH-PT-771", 8.50, 180, 30, LocalDate.now().plusMonths(20)));
            medicineRepository.save(new Medicine("Salbutamol Inhaler 100mcg", "Salbutamol", "Respiratory", DosageForm.INHALER, "100mcg/dose", "Cipla Ltd", "BATCH-SB-330", 165.00, 42, 15, LocalDate.now().plusMonths(9)));
            medicineRepository.save(new Medicine("Azithromycin 500mg", "Azithromycin", "Antibiotic", DosageForm.TABLET, "500mg", "Zydus Cadila", "BATCH-AZ-202", 24.00, 95, 20, LocalDate.now().plusMonths(15)));
            logger.info("Seeded pharmacy medicine catalog and stock counts.");
        }

        // Seed Lab Tests
        if (labTestRepository.count() == 0 && !patients.isEmpty()) {
            Patient p1 = patients.get(0);
            Doctor doc1 = !doctorRepository.findAll().isEmpty() ? doctorRepository.findAll().get(0) : null;

            LabTest t1 = new LabTest("LAB-CBC-101", "Complete Blood Count (CBC)", "Hematology", p1, doc1, "4.5 - 11.0 x10^3/uL", 450.0);
            t1.setStatus(LabTestStatus.COMPLETED);
            t1.setCollectionDate(java.time.LocalDateTime.now().minusDays(1));
            t1.setCompletionDate(java.time.LocalDateTime.now().minusHours(4));
            t1.setResultValue("7.8 x10^3/uL");
            t1.setUnit("x10^3/uL");
            t1.setInterpretation("Normal");
            t1.setRemarks("WBC count within healthy reference margins.");
            labTestRepository.save(t1);

            LabTest t2 = new LabTest("LAB-LIP-204", "Lipid Profile Panel", "Biochemistry", p1, doc1, "< 200 mg/dL", 750.0);
            t2.setStatus(LabTestStatus.COMPLETED);
            t2.setCollectionDate(java.time.LocalDateTime.now().minusDays(1));
            t2.setCompletionDate(java.time.LocalDateTime.now().minusHours(2));
            t2.setResultValue("215 mg/dL");
            t2.setUnit("mg/dL");
            t2.setInterpretation("Elevated");
            t2.setRemarks("Mild hypercholesterolemia. Dietary modification and statin dosage review advised.");
            labTestRepository.save(t2);

            LabTest t3 = new LabTest("LAB-HBA-308", "HbA1c Glycated Hemoglobin", "Endocrinology", p1, doc1, "< 5.7 %", 600.0);
            t3.setStatus(LabTestStatus.SAMPLE_COLLECTED);
            t3.setCollectionDate(java.time.LocalDateTime.now().minusHours(1));
            t3.setRemarks("Fasting blood sample processed in central analyzer.");
            labTestRepository.save(t3);

            logger.info("Seeded clinical lab test orders and diagnostic reports.");
        }

        // 13. Seed Emergency & Trauma Cases
        if (emergencyCaseRepository.count() == 0) {
            EmergencyCase er1 = new EmergencyCase();
            er1.setCaseNumber("ER-901");
            er1.setPatientName("Robert Garcia");
            er1.setPatientAge(58);
            er1.setGender("Male");
            er1.setTriageLevel(EmergencyCase.TriageLevel.RESUSCITATION);
            er1.setChiefComplaint("Acute substernal crushing chest pain, diaphoresis, ST-elevation on 12-lead ECG");
            er1.setHeartRate(132);
            er1.setBloodPressure("85/52");
            er1.setSpo2(89);
            er1.setTemperature(98.4);
            er1.setStatus(EmergencyCase.EmergencyStatus.IN_TREATMENT);
            er1.setAssignedDoctorName("Dr. David Smith");
            er1.setAssignedBedNumber("ER-Bay-1 (Resus)");
            er1.setCodeBlueTriggered(true);
            er1.setArrivalTime(java.time.LocalDateTime.now().minusMinutes(25));
            emergencyCaseRepository.save(er1);

            EmergencyCase er2 = new EmergencyCase();
            er2.setCaseNumber("ER-902");
            er2.setPatientName("Maria Rodriguez");
            er2.setPatientAge(32);
            er2.setGender("Female");
            er2.setTriageLevel(EmergencyCase.TriageLevel.EMERGENT);
            er2.setChiefComplaint("Severe lower right quadrant pain with rebound guarding, nausea & high fever");
            er2.setHeartRate(104);
            er2.setBloodPressure("132/86");
            er2.setSpo2(98);
            er2.setTemperature(101.9);
            er2.setStatus(EmergencyCase.EmergencyStatus.IN_TREATMENT);
            er2.setAssignedDoctorName("Dr. Michael Chang");
            er2.setAssignedBedNumber("ER-Bay-3");
            er2.setArrivalTime(java.time.LocalDateTime.now().minusMinutes(50));
            emergencyCaseRepository.save(er2);

            EmergencyCase er3 = new EmergencyCase();
            er3.setCaseNumber("ER-903");
            er3.setPatientName("Liam Walker");
            er3.setPatientAge(24);
            er3.setGender("Male");
            er3.setTriageLevel(EmergencyCase.TriageLevel.URGENT);
            er3.setChiefComplaint("Suspected Colles wrist fracture following bicycle crash; neurovascular intact");
            er3.setHeartRate(82);
            er3.setBloodPressure("120/78");
            er3.setSpo2(99);
            er3.setTemperature(98.6);
            er3.setStatus(EmergencyCase.EmergencyStatus.TRIAGED);
            er3.setAssignedBedNumber("ER-Bay-5");
            er3.setArrivalTime(java.time.LocalDateTime.now().minusMinutes(12));
            emergencyCaseRepository.save(er3);

            EmergencyCase er4 = new EmergencyCase();
            er4.setCaseNumber("ER-904");
            er4.setPatientName("Inbound Trauma Unit 4");
            er4.setPatientAge(45);
            er4.setGender("Male");
            er4.setTriageLevel(EmergencyCase.TriageLevel.RESUSCITATION);
            er4.setChiefComplaint("High-velocity MVA roll-over; severe chest trauma, intubated en route");
            er4.setAmbulanceNumber("AMB-104");
            er4.setEtaMinutes(4);
            er4.setStatus(EmergencyCase.EmergencyStatus.TRIAGED);
            er4.setArrivalTime(java.time.LocalDateTime.now());
            emergencyCaseRepository.save(er4);

            logger.info("Seeded Emergency & Trauma cases and inbound ambulance tracking.");
        }

        // 14. Seed Operation Theater / Surgery Schedules
        if (surgeryScheduleRepository.count() == 0) {
            SurgerySchedule s1 = new SurgerySchedule();
            s1.setSurgeryNumber("SURG-101");
            s1.setPatientName("Maria Rodriguez");
            s1.setPatientId(2L);
            s1.setLeadSurgeonName("Dr. Michael Chang");
            s1.setAnesthesiologistName("Dr. Sarah Jenkins");
            s1.setScrubNurseName("Nurse Jessica Alba");
            s1.setOtRoom("OT-1 (General Surgery Suite)");
            s1.setProcedureName("Emergency Laparoscopic Appendectomy");
            s1.setSurgeryDate(LocalDate.now());
            s1.setScheduledStartTime("04:30 PM");
            s1.setEstimatedDurationHours(1.5);
            s1.setStatus(SurgerySchedule.SurgeryStatus.SCHEDULED);
            s1.setPreOpCleared(true);
            s1.setAnesthesiaCleared(true);
            s1.setConsentSigned(true);
            s1.setBloodReserved(true);
            s1.setSurgicalNotes("NPO status confirmed from 08:00 AM. IV Cefazolin prophylaxis ordered.");
            surgeryScheduleRepository.save(s1);

            SurgerySchedule s2 = new SurgerySchedule();
            s2.setSurgeryNumber("SURG-102");
            s2.setPatientName("Robert Garcia");
            s2.setPatientId(1L);
            s2.setLeadSurgeonName("Dr. David Smith");
            s2.setAnesthesiologistName("Dr. Sarah Jenkins");
            s2.setScrubNurseName("Nurse Kevin Taylor");
            s2.setOtRoom("OT-3 (Cardiac Cath Lab)");
            s2.setProcedureName("Percutaneous Coronary Angioplasty (PTCA) + Stenting");
            s2.setSurgeryDate(LocalDate.now());
            s2.setScheduledStartTime("06:00 PM");
            s2.setEstimatedDurationHours(2.0);
            s2.setStatus(SurgerySchedule.SurgeryStatus.IN_PROGRESS);
            s2.setPreOpCleared(true);
            s2.setAnesthesiaCleared(true);
            s2.setConsentSigned(true);
            s2.setBloodReserved(true);
            s2.setSurgicalNotes("STEMI protocol activated. Dual antiplatelet loading complete.");
            surgeryScheduleRepository.save(s2);

            SurgerySchedule s3 = new SurgerySchedule();
            s3.setSurgeryNumber("SURG-103");
            s3.setPatientName("James Wilson");
            s3.setPatientId(3L);
            s3.setLeadSurgeonName("Dr. Michael Chang");
            s3.setAnesthesiologistName("Dr. Priya Sharma");
            s3.setScrubNurseName("Nurse Jessica Alba");
            s3.setOtRoom("OT-2 (Ortho/Joint Suite)");
            s3.setProcedureName("Total Knee Arthroplasty (TKR)");
            s3.setSurgeryDate(LocalDate.now().plusDays(2));
            s3.setScheduledStartTime("09:00 AM");
            s3.setEstimatedDurationHours(3.0);
            s3.setStatus(SurgerySchedule.SurgeryStatus.SCHEDULED);
            s3.setPreOpCleared(true);
            s3.setConsentSigned(true);
            surgeryScheduleRepository.save(s3);

            SurgerySchedule s4 = new SurgerySchedule();
            s4.setSurgeryNumber("SURG-104");
            s4.setPatientName("Emily Davis");
            s4.setPatientId(4L);
            s4.setLeadSurgeonName("Dr. Priya Sharma");
            s4.setAnesthesiologistName("Dr. Sarah Jenkins");
            s4.setScrubNurseName("Nurse Kevin Taylor");
            s4.setOtRoom("OT-4 (Day Care / Laparoscopy)");
            s4.setProcedureName("Diagnostic Laparoscopy & Excisional Biopsy");
            s4.setSurgeryDate(LocalDate.now());
            s4.setScheduledStartTime("11:00 AM");
            s4.setEstimatedDurationHours(1.0);
            s4.setStatus(SurgerySchedule.SurgeryStatus.IN_PACU);
            s4.setPreOpCleared(true);
            s4.setAnesthesiaCleared(true);
            s4.setConsentSigned(true);
            s4.setPacuRecoveryScore(9);
            s4.setSurgicalNotes("Procedure uneventful. Hemostasis achieved. Patient awake and comfortable in PACU.");
            surgeryScheduleRepository.save(s4);

            logger.info("Seeded Operation Theater (OT) surgical schedules and PACU tracking.");
        }

        // 15. Seed Blood Bank Inventory across 8 blood groups
        if (bloodInventoryRepository.count() == 0) {
            String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            int[] wholeUnits = {18, 6, 22, 5, 12, 3, 35, 8};
            int[] prbcUnits = {14, 4, 16, 3, 8, 2, 28, 6};

            for (int i = 0; i < groups.length; i++) {
                BloodInventory wb = new BloodInventory();
                wb.setBloodGroup(groups[i]);
                wb.setComponentType(BloodInventory.ComponentType.WHOLE_BLOOD);
                wb.setUnitsAvailable(wholeUnits[i]);
                wb.setReservedUnits(groups[i].equals("O+") ? 4 : (groups[i].equals("A+") ? 2 : 0));
                wb.setExpiryDate(LocalDate.now().plusDays(25 + i));
                wb.setStorageLocation("Blood Bank Refrigerator Bay-" + ((i % 4) + 1));
                bloodInventoryRepository.save(wb);

                BloodInventory prbc = new BloodInventory();
                prbc.setBloodGroup(groups[i]);
                prbc.setComponentType(BloodInventory.ComponentType.PRBC);
                prbc.setUnitsAvailable(prbcUnits[i]);
                prbc.setReservedUnits(groups[i].equals("O+") ? 2 : 0);
                prbc.setExpiryDate(LocalDate.now().plusDays(30 + i));
                prbc.setStorageLocation("Cold Cell Vault B-" + ((i % 4) + 1));
                bloodInventoryRepository.save(prbc);
            }

            // Platelets & FFP for high-demand groups
            BloodInventory pltO = new BloodInventory();
            pltO.setBloodGroup("O+");
            pltO.setComponentType(BloodInventory.ComponentType.PLATELETS);
            pltO.setUnitsAvailable(8);
            pltO.setReservedUnits(1);
            pltO.setExpiryDate(LocalDate.now().plusDays(4)); // Short shelf-life (5 days)
            pltO.setStorageLocation("Platelet Agitator Room 2");
            bloodInventoryRepository.save(pltO);

            BloodInventory ffpAB = new BloodInventory();
            ffpAB.setBloodGroup("AB+");
            ffpAB.setComponentType(BloodInventory.ComponentType.FFP);
            ffpAB.setUnitsAvailable(10);
            ffpAB.setReservedUnits(0);
            ffpAB.setExpiryDate(LocalDate.now().plusDays(180)); // Deep-freeze shelf life
            ffpAB.setStorageLocation("Deep Freezer -40°C Compartment A");
            bloodInventoryRepository.save(ffpAB);

            logger.info("Seeded Blood Bank inventory across all 8 major blood groups.");
        }

        // 16. Seed Insurance Claims
        if (insuranceClaimRepository.count() == 0) {
            InsuranceClaim c1 = new InsuranceClaim();
            c1.setClaimNumber("CLM-801");
            c1.setPatientName("John Doe");
            c1.setPatientId(1L);
            c1.setBillId(1L);
            c1.setPolicyNumber("BCBS-98214812");
            c1.setInsuranceProvider("BlueCross BlueShield");
            c1.setTpaName("MediAssist TPA");
            c1.setTotalBillAmount(new BigDecimal("1250.00"));
            c1.setClaimAmount(new BigDecimal("1125.00"));
            c1.setApprovedAmount(new BigDecimal("1125.00"));
            c1.setPatientCoPay(new BigDecimal("125.00"));
            c1.setStatus(InsuranceClaim.ClaimStatus.PRE_AUTH_APPROVED);
            c1.setIcdCode("I20.9 Angina Pectoris");
            c1.setClaimNotes("Pre-authorization approved for cardiology consultation and inpatient workup.");
            insuranceClaimRepository.save(c1);

            InsuranceClaim c2 = new InsuranceClaim();
            c2.setClaimNumber("CLM-802");
            c2.setPatientName("Maria Rodriguez");
            c2.setPatientId(2L);
            c2.setBillId(2L);
            c2.setPolicyNumber("AETNA-7729104");
            c2.setInsuranceProvider("Aetna Global Health");
            c2.setTpaName("Vidal Health TPA");
            c2.setTotalBillAmount(new BigDecimal("3450.00"));
            c2.setClaimAmount(new BigDecimal("3105.00"));
            c2.setPatientCoPay(new BigDecimal("345.00"));
            c2.setStatus(InsuranceClaim.ClaimStatus.UNDER_REVIEW);
            c2.setIcdCode("K35.80 Acute Appendicitis");
            c2.setClaimNotes("Surgical documentation and pre-op clearance reports submitted to insurer review desk.");
            insuranceClaimRepository.save(c2);

            InsuranceClaim c3 = new InsuranceClaim();
            c3.setClaimNumber("CLM-803");
            c3.setPatientName("Robert Garcia");
            c3.setPatientId(3L);
            c3.setPolicyNumber("STAR-0029411");
            c3.setInsuranceProvider("Star Health & Allied");
            c3.setTpaName("Direct Cashless Desk");
            c3.setTotalBillAmount(new BigDecimal("5800.00"));
            c3.setClaimAmount(new BigDecimal("5220.00"));
            c3.setPatientCoPay(new BigDecimal("580.00"));
            c3.setStatus(InsuranceClaim.ClaimStatus.SUBMITTED);
            c3.setIcdCode("I21.9 Acute Myocardial Infarction");
            c3.setClaimNotes("Emergency admission cashless authorization requested.");
            insuranceClaimRepository.save(c3);

            logger.info("Seeded Insurance Claims & TPA pre-authorization records.");
        }

        // 17. Seed Staff Shifts
        if (staffShiftRepository.count() == 0) {
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

            staffShiftRepository.save(createShift("Dr. David Smith", "Senior Cardiologist", "Cardiology", "Monday", StaffShift.ShiftType.MORNING, "08:00 AM", "04:00 PM", true, "+1 555-0192"));
            staffShiftRepository.save(createShift("Dr. Michael Chang", "Lead Trauma Surgeon", "General & Trauma Surgery", "Monday", StaffShift.ShiftType.EVENING, "04:00 PM", "12:00 AM", true, "+1 555-0193"));
            staffShiftRepository.save(createShift("Dr. Priya Sharma", "Pediatric Consultant", "Pediatrics", "Tuesday", StaffShift.ShiftType.MORNING, "08:00 AM", "04:00 PM", false, "+1 555-0194"));
            staffShiftRepository.save(createShift("Nurse Sarah Jenkins", "ICU Charge Nurse", "Critical Care Unit", "Monday", StaffShift.ShiftType.MORNING, "07:00 AM", "03:30 PM", false, "+1 555-0195"));
            staffShiftRepository.save(createShift("Nurse Kevin Taylor", "Emergency Triage Lead", "Emergency & Trauma", "Monday", StaffShift.ShiftType.NIGHT, "11:00 PM", "07:30 AM", true, "+1 555-0196"));

            for (String day : days) {
                if (!day.equals("Monday") && !day.equals("Tuesday")) {
                    staffShiftRepository.save(createShift("Dr. David Smith", "Senior Cardiologist", "Cardiology", day, StaffShift.ShiftType.MORNING, "08:00 AM", "04:00 PM", false, "+1 555-0192"));
                    staffShiftRepository.save(createShift("Dr. Michael Chang", "Lead Trauma Surgeon", "General & Trauma Surgery", day, StaffShift.ShiftType.MORNING, "08:00 AM", "04:00 PM", true, "+1 555-0193"));
                }
            }
            logger.info("Seeded Staff Shift Rostering & On-Call Specialist Directory.");
        }

        // 18. Seed Notification Dispatch Logs
        if (notificationLogRepository.count() == 0) {
            NotificationLog n1 = new NotificationLog();
            n1.setRecipientName("John Doe");
            n1.setRecipientContact("+1 555-0101");
            n1.setChannel(NotificationLog.NotificationChannel.WHATSAPP);
            n1.setTriggerEvent("APPOINTMENT_CONFIRMED");
            n1.setSubject("Appointment Confirmed with Dr. David Smith");
            n1.setMessage("Hello John, your consultation with Dr. David Smith is confirmed for tomorrow at 10:00 AM in Room 302. Please arrive 15 minutes early.");
            n1.setStatus(NotificationLog.DeliveryStatus.DELIVERED);
            n1.setSentAt(java.time.LocalDateTime.now().minusHours(3));
            notificationLogRepository.save(n1);

            NotificationLog n2 = new NotificationLog();
            n2.setRecipientName("Emma Watson");
            n2.setRecipientContact("+1 555-0102");
            n2.setChannel(NotificationLog.NotificationChannel.SMS);
            n2.setTriggerEvent("LAB_REPORT_READY");
            n2.setSubject("Pathology Results Published");
            n2.setMessage("Your CBC Complete Blood Count results are verified and published. View your report securely in the patient portal: https://hospital.health/portal");
            n2.setStatus(NotificationLog.DeliveryStatus.DELIVERED);
            n2.setSentAt(java.time.LocalDateTime.now().minusHours(1));
            notificationLogRepository.save(n2);

            NotificationLog n3 = new NotificationLog();
            n3.setRecipientName("Robert Garcia");
            n3.setRecipientContact("+1 555-0103");
            n3.setChannel(NotificationLog.NotificationChannel.SMS);
            n3.setTriggerEvent("MEDICINE_READY");
            n3.setSubject("Prescription Ready for Pickup");
            n3.setMessage("Your medications (Atorvastatin 20mg) have been dispensed and are ready for pickup at Pharmacy Counter A.");
            n3.setStatus(NotificationLog.DeliveryStatus.DELIVERED);
            n3.setSentAt(java.time.LocalDateTime.now().minusMinutes(35));
            notificationLogRepository.save(n3);

            NotificationLog n4 = new NotificationLog();
            n4.setRecipientName("Hospital Rapid Response Team");
            n4.setRecipientContact("emergency-pager-system");
            n4.setChannel(NotificationLog.NotificationChannel.BROADCAST_ALARM);
            n4.setTriggerEvent("CODE_BLUE_ALERT");
            n4.setSubject("🚨 CODE BLUE BROADCAST: ER-Bay-1");
            n4.setMessage("Cardiac arrest resus protocol activated for Patient Robert Garcia in ER-Bay-1. All available resuscitation personnel responded.");
            n4.setStatus(NotificationLog.DeliveryStatus.DELIVERED);
            n4.setSentAt(java.time.LocalDateTime.now().minusMinutes(20));
            notificationLogRepository.save(n4);

            logger.info("Seeded Automated Notification Dispatch logs.");
        }

        logger.info("Data initialization completed successfully.");
    }

    private StaffShift createShift(String name, String role, String dept, String day, StaffShift.ShiftType type, String start, String end, boolean onCall, String phone) {
        StaffShift shift = new StaffShift();
        shift.setStaffName(name);
        shift.setRoleTitle(role);
        shift.setDepartment(dept);
        shift.setDayOfWeek(day);
        shift.setShiftType(type);
        shift.setStartTime(start);
        shift.setEndTime(end);
        shift.setOnCall(onCall);
        shift.setContactPhone(phone);
        return shift;
    }
}
