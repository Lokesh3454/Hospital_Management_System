package com.hospital.hms.dto;

import com.hospital.hms.entity.Bed;
import com.hospital.hms.entity.BedStatus;
import com.hospital.hms.entity.WardType;
import java.time.LocalDateTime;

public class BedDTO {

    private Long id;
    private String bedNumber;
    private WardType wardType;
    private String roomNumber;
    private BedStatus status;
    private Double dailyRate;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private LocalDateTime admissionDate;
    private String notes;

    public BedDTO() {}

    public static BedDTO fromEntity(Bed b) {
        if (b == null) return null;
        BedDTO dto = new BedDTO();
        dto.setId(b.getId());
        dto.setBedNumber(b.getBedNumber());
        dto.setWardType(b.getWardType());
        dto.setRoomNumber(b.getRoomNumber());
        dto.setStatus(b.getStatus());
        dto.setDailyRate(b.getDailyRate());
        if (b.getCurrentPatient() != null) {
            dto.setPatientId(b.getCurrentPatient().getId());
            if (b.getCurrentPatient().getUser() != null) {
                dto.setPatientName(b.getCurrentPatient().getUser().getFirstName() + " " + b.getCurrentPatient().getUser().getLastName());
                dto.setPatientPhone(b.getCurrentPatient().getUser().getPhone());
            }
        }
        dto.setAdmissionDate(b.getAdmissionDate());
        dto.setNotes(b.getNotes());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public WardType getWardType() {
        return wardType;
    }

    public void setWardType(WardType wardType) {
        this.wardType = wardType;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public BedStatus getStatus() {
        return status;
    }

    public void setStatus(BedStatus status) {
        this.status = status;
    }

    public Double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(Double dailyRate) {
        this.dailyRate = dailyRate;
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

    public LocalDateTime getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDateTime admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
