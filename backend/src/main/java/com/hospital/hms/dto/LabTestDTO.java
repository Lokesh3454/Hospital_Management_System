package com.hospital.hms.dto;

import com.hospital.hms.entity.LabTest;
import com.hospital.hms.entity.LabTestStatus;
import java.time.LocalDateTime;

public class LabTestDTO {

    private Long id;
    private String testCode;
    private String testName;
    private String category;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime orderDate;
    private LocalDateTime collectionDate;
    private LocalDateTime completionDate;
    private LabTestStatus status;
    private String normalRange;
    private String resultValue;
    private String unit;
    private String interpretation;
    private String remarks;
    private Double cost;

    public LabTestDTO() {}

    public static LabTestDTO fromEntity(LabTest lt) {
        if (lt == null) return null;
        LabTestDTO dto = new LabTestDTO();
        dto.setId(lt.getId());
        dto.setTestCode(lt.getTestCode());
        dto.setTestName(lt.getTestName());
        dto.setCategory(lt.getCategory());
        if (lt.getPatient() != null) {
            dto.setPatientId(lt.getPatient().getId());
            if (lt.getPatient().getUser() != null) {
                dto.setPatientName(lt.getPatient().getUser().getFirstName() + " " + lt.getPatient().getUser().getLastName());
            }
        }
        if (lt.getDoctor() != null) {
            dto.setDoctorId(lt.getDoctor().getId());
            if (lt.getDoctor().getUser() != null) {
                dto.setDoctorName("Dr. " + lt.getDoctor().getUser().getFirstName() + " " + lt.getDoctor().getUser().getLastName());
            }
        }
        dto.setOrderDate(lt.getOrderDate());
        dto.setCollectionDate(lt.getCollectionDate());
        dto.setCompletionDate(lt.getCompletionDate());
        dto.setStatus(lt.getStatus());
        dto.setNormalRange(lt.getNormalRange());
        dto.setResultValue(lt.getResultValue());
        dto.setUnit(lt.getUnit());
        dto.setInterpretation(lt.getInterpretation());
        dto.setRemarks(lt.getRemarks());
        dto.setCost(lt.getCost());
        return dto;
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
