package com.hospital.hms.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class RescheduleRequestDTO {

    @NotNull(message = "New appointment date is required")
    private LocalDate appointmentDate;

    @NotNull(message = "New appointment time is required")
    private LocalTime appointmentTime;

    private String reason;
    private String notes;

    public RescheduleRequestDTO() {
    }

    public RescheduleRequestDTO(LocalDate appointmentDate, LocalTime appointmentTime, String reason, String notes) {
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.notes = notes;
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
}
