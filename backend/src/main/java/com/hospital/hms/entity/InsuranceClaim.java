package com.hospital.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_claims")
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String claimNumber;

    @Column(nullable = false)
    private String patientName;

    private Long patientId;
    private Long billId;

    @Column(nullable = false)
    private String policyNumber;

    @Column(nullable = false)
    private String insuranceProvider;

    private String tpaName = "Direct Insurer Desk";

    @Column(nullable = false)
    private BigDecimal totalBillAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal claimAmount = BigDecimal.ZERO;

    private BigDecimal approvedAmount = BigDecimal.ZERO;
    private BigDecimal patientCoPay = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    private String icdCode; // ICD-10 Diagnosis code

    @Column(columnDefinition = "TEXT")
    private String claimNotes;

    private LocalDate submissionDate = LocalDate.now();
    private LocalDate settlementDate;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ClaimStatus {
        SUBMITTED,
        PRE_AUTH_APPROVED,
        UNDER_REVIEW,
        SETTLED,
        REJECTED
    }

    public InsuranceClaim() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }

    public String getTpaName() { return tpaName; }
    public void setTpaName(String tpaName) { this.tpaName = tpaName; }

    public BigDecimal getTotalBillAmount() { return totalBillAmount; }
    public void setTotalBillAmount(BigDecimal totalBillAmount) { this.totalBillAmount = totalBillAmount; }

    public BigDecimal getClaimAmount() { return claimAmount; }
    public void setClaimAmount(BigDecimal claimAmount) { this.claimAmount = claimAmount; }

    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }

    public BigDecimal getPatientCoPay() { return patientCoPay; }
    public void setPatientCoPay(BigDecimal patientCoPay) { this.patientCoPay = patientCoPay; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public String getIcdCode() { return icdCode; }
    public void setIcdCode(String icdCode) { this.icdCode = icdCode; }

    public String getClaimNotes() { return claimNotes; }
    public void setClaimNotes(String claimNotes) { this.claimNotes = claimNotes; }

    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }

    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
