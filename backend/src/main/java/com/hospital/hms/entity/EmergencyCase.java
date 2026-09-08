package com.hospital.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_cases")
public class EmergencyCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String caseNumber;

    @Column(nullable = false)
    private String patientName;

    private Integer patientAge;

    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriageLevel triageLevel = TriageLevel.URGENT;

    @Column(columnDefinition = "TEXT")
    private String chiefComplaint;

    private Integer heartRate;
    private String bloodPressure;
    private Integer spo2;
    private Double temperature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyStatus status = EmergencyStatus.TRIAGED;

    private String assignedDoctorName;
    private String assignedBedNumber;
    private String ambulanceNumber;
    private Integer etaMinutes;
    private Boolean codeBlueTriggered = false;

    private LocalDateTime arrivalTime;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TriageLevel {
        RESUSCITATION, // ESI Level 1 (Immediate / Red)
        EMERGENT,      // ESI Level 2 (Orange)
        URGENT,        // ESI Level 3 (Yellow)
        LESS_URGENT,   // ESI Level 4 (Green)
        NON_URGENT     // ESI Level 5 (Blue)
    }

    public enum EmergencyStatus {
        TRIAGED,
        IN_TREATMENT,
        ADMITTED,
        DISCHARGED
    }

    public EmergencyCase() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public TriageLevel getTriageLevel() { return triageLevel; }
    public void setTriageLevel(TriageLevel triageLevel) { this.triageLevel = triageLevel; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    public Integer getSpo2() { return spo2; }
    public void setSpo2(Integer spo2) { this.spo2 = spo2; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public EmergencyStatus getStatus() { return status; }
    public void setStatus(EmergencyStatus status) { this.status = status; }

    public String getAssignedDoctorName() { return assignedDoctorName; }
    public void setAssignedDoctorName(String assignedDoctorName) { this.assignedDoctorName = assignedDoctorName; }

    public String getAssignedBedNumber() { return assignedBedNumber; }
    public void setAssignedBedNumber(String assignedBedNumber) { this.assignedBedNumber = assignedBedNumber; }

    public String getAmbulanceNumber() { return ambulanceNumber; }
    public void setAmbulanceNumber(String ambulanceNumber) { this.ambulanceNumber = ambulanceNumber; }

    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }

    public Boolean getCodeBlueTriggered() { return codeBlueTriggered; }
    public void setCodeBlueTriggered(Boolean codeBlueTriggered) { this.codeBlueTriggered = codeBlueTriggered; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
