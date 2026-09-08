package com.hospital.hms.dto.dashboard;

import com.hospital.hms.dto.AppointmentResponseDTO;
import com.hospital.hms.dto.BillResponseDTO;
import java.util.ArrayList;
import java.util.List;

public class PatientDashboardDTO {

    private AppointmentResponseDTO upcomingAppointment;
    private long upcomingAppointments;
    private long appointmentHistory;
    private long medicalRecords;
    private long prescriptions;
    private long pendingBills;

    private List<BillResponseDTO> pendingBillsList = new ArrayList<>();
    private List<AppointmentResponseDTO> recentAppointmentsList = new ArrayList<>();

    public PatientDashboardDTO() {}

    public AppointmentResponseDTO getUpcomingAppointment() {
        return upcomingAppointment;
    }

    public void setUpcomingAppointment(AppointmentResponseDTO upcomingAppointment) {
        this.upcomingAppointment = upcomingAppointment;
    }

    public long getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(long upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }

    public long getAppointmentHistory() {
        return appointmentHistory;
    }

    public void setAppointmentHistory(long appointmentHistory) {
        this.appointmentHistory = appointmentHistory;
    }

    public long getMedicalRecords() {
        return medicalRecords;
    }

    public void setMedicalRecords(long medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    public long getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(long prescriptions) {
        this.prescriptions = prescriptions;
    }

    public long getPendingBills() {
        return pendingBills;
    }

    public void setPendingBills(long pendingBills) {
        this.pendingBills = pendingBills;
    }

    public List<BillResponseDTO> getPendingBillsList() {
        return pendingBillsList;
    }

    public void setPendingBillsList(List<BillResponseDTO> pendingBillsList) {
        this.pendingBillsList = pendingBillsList;
    }

    public List<AppointmentResponseDTO> getRecentAppointmentsList() {
        return recentAppointmentsList;
    }

    public void setRecentAppointmentsList(List<AppointmentResponseDTO> recentAppointmentsList) {
        this.recentAppointmentsList = recentAppointmentsList;
    }
}
