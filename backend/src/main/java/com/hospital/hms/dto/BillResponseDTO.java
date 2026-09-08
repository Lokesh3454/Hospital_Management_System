package com.hospital.hms.dto;

import com.hospital.hms.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BillResponseDTO {

    private Long id;
    private Long billId; // alias for clarity
    private String billNumber;

    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    private String patientAddress;
    private String patientBloodGroup;

    private Long appointmentId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String doctorName;
    private String doctorSpecialization;

    private BigDecimal consultationFee;
    private BigDecimal medicineCharges;
    private BigDecimal testCharges;
    private BigDecimal otherCharges;
    private BigDecimal totalAmount;

    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDateTime billingDate;
    private String notes;

    public BillResponseDTO() {}

    public static BillResponseDTO fromEntity(com.hospital.hms.entity.Bill bill) {
        if (bill == null) return null;
        BillResponseDTO dto = new BillResponseDTO();
        dto.setId(bill.getId());
        dto.setBillId(bill.getId());
        dto.setBillNumber(bill.getBillNumber());
        dto.setConsultationFee(bill.getConsultationFee());
        dto.setMedicineCharges(bill.getMedicineCharges());
        dto.setTestCharges(bill.getTestCharges());
        dto.setOtherCharges(bill.getOtherCharges());
        dto.setTotalAmount(bill.getTotalAmount());
        dto.setPaymentStatus(bill.getPaymentStatus());
        dto.setPaymentMethod(bill.getPaymentMethod());
        dto.setBillingDate(bill.getBillingDate());
        dto.setNotes(bill.getNotes());

        if (bill.getPatient() != null) {
            dto.setPatientId(bill.getPatient().getId());
            if (bill.getPatient().getUser() != null) {
                dto.setPatientName(bill.getPatient().getUser().getFirstName() + " " + bill.getPatient().getUser().getLastName());
                dto.setPatientPhone(bill.getPatient().getUser().getPhone());
                dto.setPatientEmail(bill.getPatient().getUser().getEmail());
            }
            dto.setPatientAddress(bill.getPatient().getAddress());
            dto.setPatientBloodGroup(bill.getPatient().getBloodGroup());
        }

        if (bill.getAppointment() != null) {
            dto.setAppointmentId(bill.getAppointment().getId());
            dto.setAppointmentDate(bill.getAppointment().getAppointmentDate());
            dto.setAppointmentTime(bill.getAppointment().getAppointmentTime());
            if (bill.getAppointment().getDoctor() != null) {
                if (bill.getAppointment().getDoctor().getUser() != null) {
                    dto.setDoctorName("Dr. " + bill.getAppointment().getDoctor().getUser().getFirstName() + " " + bill.getAppointment().getDoctor().getUser().getLastName());
                }
                dto.setDoctorSpecialization(bill.getAppointment().getDoctor().getSpecialization());
            }
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        this.billId = id;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
        this.id = billId;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
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

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public String getPatientBloodGroup() {
        return patientBloodGroup;
    }

    public void setPatientBloodGroup(String patientBloodGroup) {
        this.patientBloodGroup = patientBloodGroup;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
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
