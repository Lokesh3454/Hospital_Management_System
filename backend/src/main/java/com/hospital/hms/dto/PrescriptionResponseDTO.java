package com.hospital.hms.dto;

import com.hospital.hms.entity.Prescription;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrescriptionResponseDTO {

    private Long id;

    // Patient info
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    private String patientGender;

    // Doctor info
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String doctorDepartment;
    private String doctorRoomNumber;

    // Appointment info
    private Long appointmentId;
    private String appointmentReason;

    // Prescription schedule & details
    private LocalDate prescriptionDate;
    private String notes;
    private List<PrescriptionItemDTO> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PrescriptionResponseDTO() {
    }

    public static PrescriptionResponseDTO fromEntity(Prescription p) {
        if (p == null) return null;
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setId(p.getId());

        if (p.getPatient() != null) {
            dto.setPatientId(p.getPatient().getId());
            if (p.getPatient().getUser() != null) {
                dto.setPatientName(p.getPatient().getUser().getFirstName() + " " + p.getPatient().getUser().getLastName());
                dto.setPatientPhone(p.getPatient().getUser().getPhone());
                dto.setPatientEmail(p.getPatient().getUser().getEmail());
            }
            dto.setPatientGender(p.getPatient().getGender());
        }

        if (p.getDoctor() != null) {
            dto.setDoctorId(p.getDoctor().getId());
            if (p.getDoctor().getUser() != null) {
                dto.setDoctorName("Dr. " + p.getDoctor().getUser().getFirstName() + " " + p.getDoctor().getUser().getLastName());
            }
            dto.setDoctorSpecialization(p.getDoctor().getSpecialization());
            dto.setDoctorDepartment(p.getDoctor().getDepartment());
            dto.setDoctorRoomNumber(p.getDoctor().getRoomNumber());
        }

        if (p.getAppointment() != null) {
            dto.setAppointmentId(p.getAppointment().getId());
            dto.setAppointmentReason(p.getAppointment().getReason());
        }

        dto.setPrescriptionDate(p.getPrescriptionDate());
        dto.setNotes(p.getNotes());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        if (p.getItems() != null) {
            dto.setItems(p.getItems().stream()
                    .map(PrescriptionItemDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

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

    public String getDoctorRoomNumber() {
        return doctorRoomNumber;
    }

    public void setDoctorRoomNumber(String doctorRoomNumber) {
        this.doctorRoomNumber = doctorRoomNumber;
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

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PrescriptionItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemDTO> items) {
        this.items = items;
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
