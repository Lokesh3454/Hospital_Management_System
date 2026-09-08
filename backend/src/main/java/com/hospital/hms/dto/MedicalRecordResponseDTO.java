package com.hospital.hms.dto;

import com.hospital.hms.entity.MedicalRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicalRecordResponseDTO {

    private Long id;

    // Patient info
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    private String patientGender;
    private String patientBloodGroup;

    // Doctor info
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String doctorDepartment;

    // Appointment info
    private Long appointmentId;
    private String appointmentReason;

    // Clinical Details
    private String diagnosis;
    private String symptoms;
    private String treatment;
    private String testResults;
    private String notes;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MedicalRecordResponseDTO() {
    }

    public static MedicalRecordResponseDTO fromEntity(MedicalRecord r) {
        if (r == null) return null;
        MedicalRecordResponseDTO dto = new MedicalRecordResponseDTO();
        dto.setId(r.getId());

        if (r.getPatient() != null) {
            dto.setPatientId(r.getPatient().getId());
            if (r.getPatient().getUser() != null) {
                dto.setPatientName(r.getPatient().getUser().getFirstName() + " " + r.getPatient().getUser().getLastName());
                dto.setPatientPhone(r.getPatient().getUser().getPhone());
                dto.setPatientEmail(r.getPatient().getUser().getEmail());
            }
            dto.setPatientGender(r.getPatient().getGender());
            dto.setPatientBloodGroup(r.getPatient().getBloodGroup());
        }

        if (r.getDoctor() != null) {
            dto.setDoctorId(r.getDoctor().getId());
            if (r.getDoctor().getUser() != null) {
                dto.setDoctorName("Dr. " + r.getDoctor().getUser().getFirstName() + " " + r.getDoctor().getUser().getLastName());
            }
            dto.setDoctorSpecialization(r.getDoctor().getSpecialization());
            dto.setDoctorDepartment(r.getDoctor().getDepartment());
        }

        if (r.getAppointment() != null) {
            dto.setAppointmentId(r.getAppointment().getId());
            dto.setAppointmentReason(r.getAppointment().getReason());
        }

        dto.setDiagnosis(r.getDiagnosis());
        dto.setSymptoms(r.getSymptoms());
        dto.setTreatment(r.getTreatmentPlan());
        dto.setTestResults(r.getTestResults());
        dto.setNotes(r.getNotes());
        dto.setRecordDate(r.getRecordDate());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getPatientBloodGroup() {
        return patientBloodGroup;
    }

    public void setPatientBloodGroup(String patientBloodGroup) {
        this.patientBloodGroup = patientBloodGroup;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getDoctorDepartment() {
        return doctorDepartment;
    }

    public void setDoctorDepartment(String doctorDepartment) {
        this.doctorDepartment = doctorDepartment;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentReason() {
        return appointmentReason;
    }

    public void setAppointmentReason(String appointmentReason) {
        this.appointmentReason = appointmentReason;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getTestResults() {
        return testResults;
    }

    public void setTestResults(String testResults) {
        this.testResults = testResults;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
