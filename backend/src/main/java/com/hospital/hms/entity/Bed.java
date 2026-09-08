package com.hospital.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "beds")
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bed_number", nullable = false, unique = true, length = 50)
    private String bedNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "ward_type", nullable = false, length = 30)
    private WardType wardType;

    @Column(name = "room_number", length = 100)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BedStatus status;

    @Column(name = "daily_rate")
    private Double dailyRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient currentPatient;

    @Column(name = "admission_date")
    private LocalDateTime admissionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Bed() {
        this.status = BedStatus.AVAILABLE;
    }

    public Bed(String bedNumber, WardType wardType, String roomNumber, BedStatus status, Double dailyRate) {
        this.bedNumber = bedNumber;
        this.wardType = wardType;
        this.roomNumber = roomNumber;
        this.status = status != null ? status : BedStatus.AVAILABLE;
        this.dailyRate = dailyRate;
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

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public void setCurrentPatient(Patient currentPatient) {
        this.currentPatient = currentPatient;
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
