package com.hospital.hms.dto;

import java.util.ArrayList;
import java.util.List;

public class ClinicalSupportDTO {

    public static class SafetyCheckRequest {
        private List<String> medicineNames = new ArrayList<>();
        private String patientAllergies;
        private Long patientId;

        public List<String> getMedicineNames() { return medicineNames; }
        public void setMedicineNames(List<String> medicineNames) { this.medicineNames = medicineNames; }
        public String getPatientAllergies() { return patientAllergies; }
        public void setPatientAllergies(String patientAllergies) { this.patientAllergies = patientAllergies; }
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
    }

    public static class SafetyAlert {
        private String severity; // "HIGH", "MODERATE", "LOW"
        private String type;     // "DRUG_INTERACTION", "ALLERGY_CONTRAINDICATION", "DOSAGE_WARNING"
        private String title;
        private String description;
        private String recommendation;

        public SafetyAlert() {}

        public SafetyAlert(String severity, String type, String title, String description, String recommendation) {
            this.severity = severity;
            this.type = type;
            this.title = title;
            this.description = description;
            this.recommendation = recommendation;
        }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }

    public static class ClinicalBrief {
        private String patientName;
        private String ageGender;
        private String bloodGroup;
        private List<String> chronicConditions = new ArrayList<>();
        private List<String> activeAllergies = new ArrayList<>();
        private String latestVitalsSummary;
        private List<String> clinicalHighlights = new ArrayList<>();

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public String getAgeGender() { return ageGender; }
        public void setAgeGender(String ageGender) { this.ageGender = ageGender; }
        public String getBloodGroup() { return bloodGroup; }
        public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
        public List<String> getChronicConditions() { return chronicConditions; }
        public void setChronicConditions(List<String> chronicConditions) { this.chronicConditions = chronicConditions; }
        public List<String> getActiveAllergies() { return activeAllergies; }
        public void setActiveAllergies(List<String> activeAllergies) { this.activeAllergies = activeAllergies; }
        public String getLatestVitalsSummary() { return latestVitalsSummary; }
        public void setLatestVitalsSummary(String latestVitalsSummary) { this.latestVitalsSummary = latestVitalsSummary; }
        public List<String> getClinicalHighlights() { return clinicalHighlights; }
        public void setClinicalHighlights(List<String> clinicalHighlights) { this.clinicalHighlights = clinicalHighlights; }
    }
}
