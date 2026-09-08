package com.hospital.hms.dto.dashboard;

import com.hospital.hms.dto.AppointmentResponseDTO;
import com.hospital.hms.dto.MedicalRecordResponseDTO;
import java.util.ArrayList;
import java.util.List;

public class DoctorDashboardDTO {

    private long todayAppointments;
    private long upcomingAppointments;
    private long totalPatients;
    private long recentMedicalRecords;
    private long prescriptions;

    private List<AppointmentResponseDTO> todayAppointmentsList = new ArrayList<>();
    private List<AppointmentResponseDTO> upcomingAppointmentsList = new ArrayList<>();
    private List<MedicalRecordResponseDTO> recentMedicalRecordsList = new ArrayList<>();

    public DoctorDashboardDTO() {}

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public long getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(long upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getRecentMedicalRecords() {
        return recentMedicalRecords;
    }

    public void setRecentMedicalRecords(long recentMedicalRecords) {
        this.recentMedicalRecords = recentMedicalRecords;
    }

    public long getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(long prescriptions) {
        this.prescriptions = prescriptions;
    }

    public List<AppointmentResponseDTO> getTodayAppointmentsList() {
        return todayAppointmentsList;
    }

    public void setTodayAppointmentsList(List<AppointmentResponseDTO> todayAppointmentsList) {
        this.todayAppointmentsList = todayAppointmentsList;
    }

    public List<AppointmentResponseDTO> getUpcomingAppointmentsList() {
        return upcomingAppointmentsList;
    }

    public void setUpcomingAppointmentsList(List<AppointmentResponseDTO> upcomingAppointmentsList) {
        this.upcomingAppointmentsList = upcomingAppointmentsList;
    }

    public List<MedicalRecordResponseDTO> getRecentMedicalRecordsList() {
        return recentMedicalRecordsList;
    }

    public void setRecentMedicalRecordsList(List<MedicalRecordResponseDTO> recentMedicalRecordsList) {
        this.recentMedicalRecordsList = recentMedicalRecordsList;
    }
}
