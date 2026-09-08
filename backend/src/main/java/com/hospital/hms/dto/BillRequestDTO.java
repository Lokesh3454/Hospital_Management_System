package com.hospital.hms.dto;

import com.hospital.hms.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BillRequestDTO {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long appointmentId;

    private BigDecimal consultationFee = BigDecimal.ZERO;
    private BigDecimal medicineCharges = BigDecimal.ZERO;
    private BigDecimal testCharges = BigDecimal.ZERO;
    private BigDecimal otherCharges = BigDecimal.ZERO;

    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String paymentMethod;
    private LocalDate billDate;
    private String notes;

    public BillRequestDTO() {}

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public BigDecimal getMedicineCharges() {
        return medicineCharges;
    }

    public void setMedicineCharges(BigDecimal medicineCharges) {
        this.medicineCharges = medicineCharges;
    }

    public BigDecimal getTestCharges() {
        return testCharges;
    }

    public void setTestCharges(BigDecimal testCharges) {
        this.testCharges = testCharges;
    }

    public BigDecimal getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(BigDecimal otherCharges) {
        this.otherCharges = otherCharges;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
