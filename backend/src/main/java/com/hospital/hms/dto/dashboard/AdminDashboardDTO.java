package com.hospital.hms.dto.dashboard;

import com.hospital.hms.dto.AppointmentResponseDTO;
import com.hospital.hms.dto.BillResponseDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardDTO {

    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long pendingBills;
    private BigDecimal pendingBillsAmount = BigDecimal.ZERO;
    private long todayAppointments;

    private List<AppointmentResponseDTO> todayAppointmentsList = new ArrayList<>();
    private List<BillResponseDTO> recentBills = new ArrayList<>();

    public AdminDashboardDTO() {}

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getPendingBills() {
        return pendingBills;
    }

    public void setPendingBills(long pendingBills) {
        this.pendingBills = pendingBills;
    }

    public BigDecimal getPendingBillsAmount() {
        return pendingBillsAmount;
    }

    public void setPendingBillsAmount(BigDecimal pendingBillsAmount) {
        this.pendingBillsAmount = pendingBillsAmount;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public List<AppointmentResponseDTO> getTodayAppointmentsList() {
        return todayAppointmentsList;
    }

    public void setTodayAppointmentsList(List<AppointmentResponseDTO> todayAppointmentsList) {
        this.todayAppointmentsList = todayAppointmentsList;
    }

    public List<BillResponseDTO> getRecentBills() {
        return recentBills;
    }

    public void setRecentBills(List<BillResponseDTO> recentBills) {
        this.recentBills = recentBills;
    }
}
