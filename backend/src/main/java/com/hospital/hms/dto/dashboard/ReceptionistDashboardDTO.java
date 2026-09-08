package com.hospital.hms.dto.dashboard;

import com.hospital.hms.dto.AppointmentResponseDTO;
import com.hospital.hms.dto.BillResponseDTO;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistDashboardDTO {

    private long todayAppointments;
    private long patientRegistrations;
    private long appointmentBookings;
    private long pendingBills;

    private List<AppointmentResponseDTO> todayAppointmentsList = new ArrayList<>();
    private List<BillResponseDTO> recentBillsList = new ArrayList<>();

    public ReceptionistDashboardDTO() {}

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public long getPatientRegistrations() {
        return patientRegistrations;
    }

    public void setPatientRegistrations(long patientRegistrations) {
        this.patientRegistrations = patientRegistrations;
    }

    public long getAppointmentBookings() {
        return appointmentBookings;
    }

    public void setAppointmentBookings(long appointmentBookings) {
        this.appointmentBookings = appointmentBookings;
    }

    public long getPendingBills() {
        return pendingBills;
    }

    public void setPendingBills(long pendingBills) {
        this.pendingBills = pendingBills;
    }

    public List<AppointmentResponseDTO> getTodayAppointmentsList() {
        return todayAppointmentsList;
    }

    public void setTodayAppointmentsList(List<AppointmentResponseDTO> todayAppointmentsList) {
        this.todayAppointmentsList = todayAppointmentsList;
    }

    public List<BillResponseDTO> getRecentBillsList() {
        return recentBillsList;
    }

    public void setRecentBillsList(List<BillResponseDTO> recentBillsList) {
        this.recentBillsList = recentBillsList;
    }
}
