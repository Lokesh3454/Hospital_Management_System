package com.hospital.hms.dto;

import com.hospital.hms.entity.Vitals;
import java.time.LocalDateTime;

public class VitalsDTO {

    private Long id;
    private Long patientId;
    private String patientName;
    private String recordedBy;
    private Integer systolicBP;
    private Integer diastolicBP;
    private Integer heartRate;
    private Double temperature;
    private Integer spo2;
    private Integer respiratoryRate;
    private Double weightKg;
    private Double heightCm;
    private Double bmi;
    private String notes;
    private LocalDateTime recordedAt;

    public VitalsDTO() {}

    public static VitalsDTO fromEntity(Vitals v) {
        if (v == null) return null;
        VitalsDTO dto = new VitalsDTO();
        dto.setId(v.getId());
        if (v.getPatient() != null) {
            dto.setPatientId(v.getPatient().getId());
            if (v.getPatient().getUser() != null) {
                dto.setPatientName(v.getPatient().getUser().getFirstName() + " " + v.getPatient().getUser().getLastName());
            }
        }
        dto.setRecordedBy(v.getRecordedBy());
        dto.setSystolicBP(v.getSystolicBP());
        dto.setDiastolicBP(v.getDiastolicBP());
        dto.setHeartRate(v.getHeartRate());
        dto.setTemperature(v.getTemperature());
        dto.setSpo2(v.getSpo2());
        dto.setRespiratoryRate(v.getRespiratoryRate());
        dto.setWeightKg(v.getWeightKg());
        dto.setHeightCm(v.getHeightCm());
        dto.setBmi(v.getBmi());
        dto.setNotes(v.getNotes());
        dto.setRecordedAt(v.getRecordedAt());
        return dto;
    }

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

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public Integer getSystolicBP() {
        return systolicBP;
    }

    public void setSystolicBP(Integer systolicBP) {
        this.systolicBP = systolicBP;
    }

    public Integer getDiastolicBP() {
        return diastolicBP;
    }

    public void setDiastolicBP(Integer diastolicBP) {
        this.diastolicBP = diastolicBP;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getSpo2() {
        return spo2;
    }

    public void setSpo2(Integer spo2) {
        this.spo2 = spo2;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
