package com.hospital.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
public class LabTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_code", length = 50)
    private String testCode;

    @Column(name = "test_name", nullable = false, length = 150)
    private String testName;

    @Column(length = 100)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "collection_date")
    private LocalDateTime collectionDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LabTestStatus status;

    @Column(name = "normal_range", length = 100)
    private String normalRange;

    @Column(name = "result_value", length = 100)
    private String resultValue;

    @Column(length = 50)
    private String unit;

    @Column(length = 50)
    private String interpretation; // e.g. Normal, Low, Elevated, Critical

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "cost")
    private Double cost;

    public LabTest() {
        this.orderDate = LocalDateTime.now();
        this.status = LabTestStatus.ORDERED;
        this.cost = 0.0;
    }

    public LabTest(String testCode, String testName, String category, Patient patient, Doctor doctor,
                   String normalRange, Double cost) {
        this.testCode = testCode;
        this.testName = testName;
        this.category = category;
        this.patient = patient;
        this.doctor = doctor;
        this.normalRange = normalRange;
        this.cost = cost != null ? cost : 0.0;
        this.orderDate = LocalDateTime.now();
        this.status = LabTestStatus.ORDERED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDateTime collectionDate) {
        this.collectionDate = collectionDate;
    }

    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }

    public LabTestStatus getStatus() {
        return status;
    }

    public void setStatus(LabTestStatus status) {
        this.status = status;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }
}
