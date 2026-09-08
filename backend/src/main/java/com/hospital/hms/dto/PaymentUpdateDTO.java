package com.hospital.hms.dto;

import com.hospital.hms.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;

public class PaymentUpdateDTO {

    @NotBlank(message = "Payment method is required (CASH, CARD, UPI)")
    private String paymentMethod;

    private PaymentStatus paymentStatus = PaymentStatus.PAID;

    private String notes;

    public PaymentUpdateDTO() {}

    public PaymentUpdateDTO(String paymentMethod, PaymentStatus paymentStatus, String notes) {
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.notes = notes;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
