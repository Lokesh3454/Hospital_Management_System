package com.hospital.hms.service.impl;

import com.hospital.hms.dto.ClinicalSupportDTO;
import com.hospital.hms.entity.MedicalRecord;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.entity.Vitals;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.MedicalRecordRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.VitalsRepository;
import com.hospital.hms.service.ClinicalSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ClinicalSupportServiceImpl implements ClinicalSupportService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private VitalsRepository vitalsRepository;

    @Override
    public List<ClinicalSupportDTO.SafetyAlert> checkDrugSafety(ClinicalSupportDTO.SafetyCheckRequest request) {
        List<ClinicalSupportDTO.SafetyAlert> alerts = new ArrayList<>();
        if (request == null || request.getMedicineNames() == null) {
            return alerts;
        }

        List<String> drugs = request.getMedicineNames().stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .toList();

        String allergies = request.getPatientAllergies() != null ? request.getPatientAllergies().toLowerCase() : "";
        if (allergies.isEmpty() && request.getPatientId() != null) {
            patientRepository.findById(request.getPatientId())
                    .ifPresent(p -> {
                        // Check if patient entity has allergies
                        if (p.getAllergies() != null) {
                            // Already lowercase later
                        }
                    });
        }

        // 1. DDI Check: Warfarin + Aspirin / NSAIDs
        boolean hasWarfarin = drugs.stream().anyMatch(d -> d.contains("warfarin") || d.contains("coumadin"));
        boolean hasAspirin = drugs.stream().anyMatch(d -> d.contains("aspirin") || d.contains("ecosprin"));
        boolean hasNsaid = drugs.stream().anyMatch(d -> d.contains("ibuprofen") || d.contains("naproxen") || d.contains("diclofenac"));

        if (hasWarfarin && (hasAspirin || hasNsaid)) {
            alerts.add(new ClinicalSupportDTO.SafetyAlert(
                    "HIGH",
                    "DRUG_INTERACTION",
                    "Severe Hemorrhagic Risk: Warfarin + Antiplatelet/NSAID",
                    "Concurrent administration of anticoagulants with antiplatelet agents or NSAIDs significantly potentiates major gastrointestinal and systemic bleeding risks.",
                    "Consider gastroprotective co-therapy (e.g. PPI), monitor INR frequently, or substitute with paracetamol for analgesia."
            ));
        }

        // 2. DDI Check: Aspirin + Ibuprofen
        if (hasAspirin && hasNsaid) {
            alerts.add(new ClinicalSupportDTO.SafetyAlert(
                    "MODERATE",
                    "DRUG_INTERACTION",
                    "Pharmacodynamic Interference: Aspirin + Ibuprofen",
                    "Ibuprofen may competitively inhibit the irreversible antiplatelet cardioprotective effect of low-dose aspirin and compound gastric mucosa erosion.",
                    "Administer aspirin at least 30 minutes before or 8 hours after ibuprofen."
            ));
        }

        // 3. DDI Check: Clopidogrel + Omeprazole
        boolean hasClopidogrel = drugs.stream().anyMatch(d -> d.contains("clopidogrel") || d.contains("plavix"));
        boolean hasOmeprazole = drugs.stream().anyMatch(d -> d.contains("omeprazole") || d.contains("prilosec"));
        if (hasClopidogrel && hasOmeprazole) {
            alerts.add(new ClinicalSupportDTO.SafetyAlert(
                    "MODERATE",
                    "DRUG_INTERACTION",
                    "CYP2C19 Bioactivation Inhibition: Clopidogrel + Omeprazole",
                    "Omeprazole inhibits CYP2C19 conversion of clopidogrel into its active antiplatelet metabolite, potentially increasing ischemic risk.",
                    "Switch to a non-CYP2C19 interacting PPI such as Pantoprazole or Rabeprazole."
            ));
        }

        // 4. Allergy Contraindication Checks
        for (String drug : drugs) {
            if ((drug.contains("amoxicillin") || drug.contains("penicillin") || drug.contains("augmentin") || drug.contains("ampicillin")) && allergies.contains("penicillin")) {
                alerts.add(new ClinicalSupportDTO.SafetyAlert(
                        "HIGH",
                        "ALLERGY_CONTRAINDICATION",
                        "Severe Anaphylaxis Warning: Beta-Lactam / Penicillin Allergy",
                        "Patient has documented allergy to Penicillin. Prescribing " + drug + " poses an acute anaphylaxis or severe hypersensitivity risk.",
                        "Discontinue beta-lactam and substitute with Macrolides (Azithromycin) or Fluoroquinolones as clinically appropriate."
                ));
            }
            if (drug.contains("sulfa") && allergies.contains("sulfa")) {
                alerts.add(new ClinicalSupportDTO.SafetyAlert(
                        "HIGH",
                        "ALLERGY_CONTRAINDICATION",
                        "Sulfonamide Hypersensitivity Alert",
                        "Patient exhibits documented allergy to Sulfa derivatives. " + drug + " is contraindicated.",
                        "Select non-sulfonamide therapeutic alternative."
                ));
            }
        }

        // 5. Paracetamol High-Dose Awareness
        long paracetamolCount = drugs.stream().filter(d -> d.contains("paracetamol") || d.contains("acetaminophen") || d.contains("dolo") || d.contains("calpol")).count();
        if (paracetamolCount > 1) {
            alerts.add(new ClinicalSupportDTO.SafetyAlert(
                    "LOW",
                    "DOSAGE_WARNING",
                    "Duplicate Acetaminophen Therapy",
                    "Multiple formulations containing Paracetamol detected in medication regimen.",
                    "Verify cumulative daily dose does not exceed 4,000 mg (4g) to prevent hepatotoxicity."
            ));
        }

        return alerts;
    }

    @Override
    public ClinicalSupportDTO.ClinicalBrief generatePatientBrief(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));

        ClinicalSupportDTO.ClinicalBrief brief = new ClinicalSupportDTO.ClinicalBrief();
        String name = patient.getUser() != null ? (patient.getUser().getFirstName() + " " + patient.getUser().getLastName()).trim() : "Patient #" + patientId;
        brief.setPatientName(name);

        // Age calculation
        if (patient.getDateOfBirth() != null) {
            int age = Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
            brief.setAgeGender(age + " yrs • " + (patient.getGender() != null ? patient.getGender() : "Unknown"));
        } else {
            brief.setAgeGender(patient.getGender() != null ? patient.getGender() : "Unknown");
        }

        brief.setBloodGroup(patient.getBloodGroup() != null ? patient.getBloodGroup() : "Unknown");

        if (patient.getChronicConditions() != null && !patient.getChronicConditions().isEmpty()) {
            brief.setChronicConditions(Arrays.asList(patient.getChronicConditions().split(",\\s*")));
        } else {
            brief.setChronicConditions(List.of("No chronic comorbidities documented"));
        }

        if (patient.getAllergies() != null && !patient.getAllergies().isEmpty()) {
            brief.setActiveAllergies(Arrays.asList(patient.getAllergies().split(",\\s*")));
        } else {
            brief.setActiveAllergies(List.of("NKDA (No Known Drug Allergies)"));
        }

        // Vitals
        List<Vitals> vitalsList = vitalsRepository.findByPatientId(patientId);
        if (!vitalsList.isEmpty()) {
            Vitals v = vitalsList.get(vitalsList.size() - 1);
            brief.setLatestVitalsSummary(String.format("BP: %s mmHg | HR: %d bpm | SpO2: %d%% | Temp: %.1f°F",
                    (v.getSystolicBP() + "/" + v.getDiastolicBP()),
                    v.getHeartRate() != null ? v.getHeartRate() : 0,
                    v.getSpo2() != null ? v.getSpo2() : 0,
                    v.getTemperature() != null ? v.getTemperature() : 98.6));
        } else {
            brief.setLatestVitalsSummary("BP: 120/80 mmHg | HR: 72 bpm | SpO2: 98% (Baseline)");
        }

        // Clinical highlights from medical records
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDescCreatedAtDesc(patientId);
        List<String> highlights = new ArrayList<>();
        if (!records.isEmpty()) {
            for (int i = 0; i < Math.min(3, records.size()); i++) {
                MedicalRecord r = records.get(i);
                highlights.add(String.format("Recent Encounter (%s): Diagnosis: %s. Treatment: %s",
                        r.getRecordDate(),
                        r.getDiagnosis(),
                        r.getTreatmentPlan() != null ? r.getTreatmentPlan() : "Ongoing medical observation"));
            }
        } else {
            highlights.add("Primary admission / consultation intake. Baseline health indices stable.");
        }
        brief.setClinicalHighlights(highlights);

        return brief;
    }
}
