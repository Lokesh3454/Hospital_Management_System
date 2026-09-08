package com.hospital.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_number", nullable = false, unique = true, length = 50)
    private String billNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @Column(name = "medicine_charges", precision = 10, scale = 2)
    private BigDecimal medicineCharges = BigDecimal.ZERO;

    @Column(name = "test_charges", precision = 10, scale = 2)
    private BigDecimal testCharges = BigDecimal.ZERO;

    @Column(name = "other_charges", precision = 10, scale = 2)
    private BigDecimal otherCharges = BigDecimal.ZERO;

    // Legacy column kept in sync
    @Column(precision = 10, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    // Legacy column kept in sync
    @Column(precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 30, nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "billing_date")
    private LocalDateTime billingDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public Bill() {
    }

    public Bill(Patient patient, Appointment appointment, BigDecimal consultationFee,
                BigDecimal medicineCharges, BigDecimal testCharges, BigDecimal otherCharges,
                PaymentStatus paymentStatus, String paymentMethod) {
        this.patient = patient;
        this.appointment = appointment;
        this.consultationFee = consultationFee != null ? consultationFee : BigDecimal.ZERO;
        this.medicineCharges = medicineCharges != null ? medicineCharges : BigDecimal.ZERO;
        this.testCharges = testCharges != null ? testCharges : BigDecimal.ZERO;
        this.otherCharges = otherCharges != null ? otherCharges : BigDecimal.ZERO;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.PENDING;
        this.paymentMethod = paymentMethod;
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        BigDecimal cf = this.consultationFee != null ? this.consultationFee : BigDecimal.ZERO;
        BigDecimal mc = this.medicineCharges != null ? this.medicineCharges : BigDecimal.ZERO;
        BigDecimal tc = this.testCharges != null ? this.testCharges : BigDecimal.ZERO;
        BigDecimal oc = this.otherCharges != null ? this.otherCharges : BigDecimal.ZERO;
        this.totalAmount = cf.add(mc).add(tc).add(oc);
        this.amount = this.totalAmount;
        if (this.tax == null) {
            this.tax = BigDecimal.ZERO;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.billingDate == null) {
            this.billingDate = LocalDateTime.now();
        }
        if (this.billNumber == null || this.billNumber.trim().isEmpty()) {
            this.billNumber = "BILL-" + System.currentTimeMillis();
        }
        calculateTotalAmount();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotalAmount();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public LocalDateTime getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(LocalDateTime billingDate) {
        this.billingDate = billingDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
