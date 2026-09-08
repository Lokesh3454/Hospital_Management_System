package com.hospital.hms.dto;

import com.hospital.hms.entity.Appointment;
import com.hospital.hms.entity.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AppointmentResponseDTO {

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
    private BigDecimal doctorConsultationFee;

    // Appointment schedule
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String formattedTime;

    private AppointmentStatus status;
    private String reason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AppointmentResponseDTO() {
    }

    public static AppointmentResponseDTO fromEntity(Appointment a) {
        if (a == null) return null;
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(a.getId());

        if (a.getPatient() != null) {
            dto.setPatientId(a.getPatient().getId());
            if (a.getPatient().getUser() != null) {
                dto.setPatientName(a.getPatient().getUser().getFirstName() + " " + a.getPatient().getUser().getLastName());
                dto.setPatientPhone(a.getPatient().getUser().getPhone());
                dto.setPatientEmail(a.getPatient().getUser().getEmail());
            }
            dto.setPatientGender(a.getPatient().getGender());
        }

        if (a.getDoctor() != null) {
            dto.setDoctorId(a.getDoctor().getId());
            if (a.getDoctor().getUser() != null) {
                dto.setDoctorName("Dr. " + a.getDoctor().getUser().getFirstName() + " " + a.getDoctor().getUser().getLastName());
            }
            dto.setDoctorSpecialization(a.getDoctor().getSpecialization());
            dto.setDoctorDepartment(a.getDoctor().getDepartment());
            dto.setDoctorRoomNumber(a.getDoctor().getRoomNumber());
            dto.setDoctorConsultationFee(a.getDoctor().getConsultationFee());
        }

        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setAppointmentTime(a.getAppointmentTime());
        if (a.getAppointmentTime() != null) {
            dto.setFormattedTime(a.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        }
        dto.setStatus(a.getStatus());
        dto.setReason(a.getReason());
        dto.setNotes(a.getNotes());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());

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

    public BigDecimal getDoctorConsultationFee() {
        return doctorConsultationFee;
    }

    public void setDoctorConsultationFee(BigDecimal doctorConsultationFee) {
        this.doctorConsultationFee = doctorConsultationFee;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
