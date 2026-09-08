package com.hospital.hms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionRequestDTO {

    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private LocalDate prescriptionDate;
    private String notes;

    @NotEmpty(message = "At least one medicine item is required")
    @Valid
    private List<PrescriptionItemDTO> items = new ArrayList<>();

    public PrescriptionRequestDTO() {
    }

    public PrescriptionRequestDTO(Long patientId, Long doctorId, Long appointmentId,
                                  LocalDate prescriptionDate, String notes, List<PrescriptionItemDTO> items) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.prescriptionDate = prescriptionDate;
        this.notes = notes;
        this.items = items;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
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
}
