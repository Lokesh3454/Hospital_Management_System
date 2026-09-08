package com.hospital.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "surgery_schedules")
public class SurgerySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String surgeryNumber;

    @Column(nullable = false)
    private String patientName;

    private Long patientId;

    @Column(nullable = false)
    private String leadSurgeonName;

    private String anesthesiologistName;
    private String scrubNurseName;

    @Column(nullable = false)
    private String otRoom; // e.g. OT-1 (General), OT-2 (Ortho/Neuro), OT-3 (Cardiac Cath), OT-4 (Laparoscopy)

    @Column(nullable = false)
    private String procedureName;

    @Column(nullable = false)
    private LocalDate surgeryDate;

    private String scheduledStartTime; // e.g. "09:30 AM"
    private Double estimatedDurationHours = 2.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurgeryStatus status = SurgeryStatus.SCHEDULED;

    private Boolean preOpCleared = false;
    private Boolean anesthesiaCleared = false;
    private Boolean consentSigned = false;
    private Boolean bloodReserved = false;

    private Integer pacuRecoveryScore; // Aldrete Recovery Score (0-10)
    
    @Column(columnDefinition = "TEXT")
    private String surgicalNotes;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum SurgeryStatus {
        SCHEDULED,
        IN_PROGRESS,
        IN_PACU,
        COMPLETED,
        CANCELLED
    }

    public SurgerySchedule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSurgeryNumber() { return surgeryNumber; }
    public void setSurgeryNumber(String surgeryNumber) { this.surgeryNumber = surgeryNumber; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getLeadSurgeonName() { return leadSurgeonName; }
    public void setLeadSurgeonName(String leadSurgeonName) { this.leadSurgeonName = leadSurgeonName; }

    public String getAnesthesiologistName() { return anesthesiologistName; }
    public void setAnesthesiologistName(String anesthesiologistName) { this.anesthesiologistName = anesthesiologistName; }

    public String getScrubNurseName() { return scrubNurseName; }
    public void setScrubNurseName(String scrubNurseName) { this.scrubNurseName = scrubNurseName; }

    public String getOtRoom() { return otRoom; }
    public void setOtRoom(String otRoom) { this.otRoom = otRoom; }

    public String getProcedureName() { return procedureName; }
    public void setProcedureName(String procedureName) { this.procedureName = procedureName; }

    public LocalDate getSurgeryDate() { return surgeryDate; }
    public void setSurgeryDate(LocalDate surgeryDate) { this.surgeryDate = surgeryDate; }

    public String getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(String scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

    public Double getEstimatedDurationHours() { return estimatedDurationHours; }
    public void setEstimatedDurationHours(Double estimatedDurationHours) { this.estimatedDurationHours = estimatedDurationHours; }

    public SurgeryStatus getStatus() { return status; }
    public void setStatus(SurgeryStatus status) { this.status = status; }

    public Boolean getPreOpCleared() { return preOpCleared; }
    public void setPreOpCleared(Boolean preOpCleared) { this.preOpCleared = preOpCleared; }

    public Boolean getAnesthesiaCleared() { return anesthesiaCleared; }
    public void setAnesthesiaCleared(Boolean anesthesiaCleared) { this.anesthesiaCleared = anesthesiaCleared; }

    public Boolean getConsentSigned() { return consentSigned; }
    public void setConsentSigned(Boolean consentSigned) { this.consentSigned = consentSigned; }

    public Boolean getBloodReserved() { return bloodReserved; }
    public void setBloodReserved(Boolean bloodReserved) { this.bloodReserved = bloodReserved; }

    public Integer getPacuRecoveryScore() { return pacuRecoveryScore; }
    public void setPacuRecoveryScore(Integer pacuRecoveryScore) { this.pacuRecoveryScore = pacuRecoveryScore; }

    public String getSurgicalNotes() { return surgicalNotes; }
    public void setSurgicalNotes(String surgicalNotes) { this.surgicalNotes = surgicalNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
